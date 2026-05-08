package com.financial.corefinance.service.validation;

import com.financial.corefinance.domain.entity.*;
import com.financial.corefinance.exception.*;
import com.financial.corefinance.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessValidationServiceTest {

    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private JournalHeaderRepository journalHeaderRepository;
    
    @Mock
    private BudgetLineRepository budgetLineRepository;
    
    @Mock
    private AccountingPeriodRepository accountingPeriodRepository;
    
    @Mock
    private FiscalYearRepository fiscalYearRepository;

    @InjectMocks
    private BusinessValidationService businessValidationService;

    private JournalHeader testJournal;
    private Account testAccount;
    private AccountingPeriod testPeriod;
    private FiscalYear testFiscalYear;

    @BeforeEach
    void setUp() {
        // Setup test data
        testAccount = Account.builder()
            .id(UUID.randomUUID())
            .tenantId("test-tenant")
            .accountCode("1001")
            .accountName("Cash")
            .accountType(Account.AccountType.ASSET)
            .normalBalance(Account.NormalBalance.DEBIT)
            .isActive(true)
            .allowManualEntry(true)
            .build();

        testPeriod = AccountingPeriod.builder()
            .id(UUID.randomUUID())
            .tenantId("test-tenant")
            .periodNumber(1)
            .periodName("Period 1")
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2024, 1, 31))
            .isOpen(true)
            .isClosed(false)
            .build();

        testFiscalYear = FiscalYear.builder()
            .id(UUID.randomUUID())
            .tenantId("test-tenant")
            .yearNumber(2024)
            .yearName("FY 2024")
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2024, 12, 31))
            .isCurrent(true)
            .isClosed(false)
            .build();

        testPeriod.setFiscalYearId(testFiscalYear.getId());

        testJournal = JournalHeader.builder()
            .tenantId("test-tenant")
            .journalDate(LocalDate.of(2024, 1, 15))
            .accountingPeriodId(testPeriod.getId())
            .journalType(JournalHeader.JournalType.MANUAL)
            .referenceNumber("REF-001")
            .description("Test Journal")
            .narration("Test journal entry")
            .build();

        JournalLine journalLine = JournalLine.builder()
            .accountId(testAccount.getId())
            .debitAmount(BigDecimal.valueOf(1000))
            .description("Test debit entry")
            .build();

        testJournal.setJournalLines(Arrays.asList(journalLine));
    }

    @Test
    void validateJournalForPosting_ValidJournal_ShouldPass() {
        // Given
        when(accountingPeriodRepository.findById(testPeriod.getId())).thenReturn(Optional.of(testPeriod));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(fiscalYearRepository.findById(testFiscalYear.getId())).thenReturn(Optional.of(testFiscalYear));
        when(journalHeaderRepository.existsByTenantIdAndJournalNumber(eq("test-tenant"), eq("REF-001")))
            .thenReturn(false);

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> {
            businessValidationService.validateJournalForPosting(testJournal);
        });

        verify(accountingPeriodRepository).findById(testPeriod.getId());
        verify(accountRepository).findById(testAccount.getId());
    }

    @Test
    void validateJournalForPosting_EmptyJournalLines_ShouldThrowException() {
        // Given
        testJournal.setJournalLines(Collections.emptyList());

        // When & Then
        JournalPostingException exception = assertThrows(JournalPostingException.class, () -> {
            businessValidationService.validateJournalForPosting(testJournal);
        });

        assertEquals("Journal must have at least one line", exception.getMessage());
    }

    @Test
    void validateJournalForPosting_FutureDate_ShouldThrowException() {
        // Given
        testJournal.setJournalDate(LocalDate.now().plusDays(1));

        // When & Then
        JournalPostingException exception = assertThrows(JournalPostingException.class, () -> {
            businessValidationService.validateJournalForPosting(testJournal);
        });

        assertEquals("Journal date cannot be in the future", exception.getMessage());
    }

    @Test
    void validateJournalForPosting_ClosedPeriod_ShouldThrowException() {
        // Given
        testPeriod.setIsClosed(true);
        when(accountingPeriodRepository.findById(testPeriod.getId())).thenReturn(Optional.of(testPeriod));

        // When & Then
        JournalPostingException exception = assertThrows(JournalPostingException.class, () -> {
            businessValidationService.validateJournalForPosting(testJournal);
        });

        assertEquals("Accounting period is closed: " + testPeriod.getPeriodName(), exception.getMessage());
    }

    @Test
    void validateJournalForPosting_InactiveAccount_ShouldThrowException() {
        // Given
        testAccount.setIsActive(false);
        when(accountingPeriodRepository.findById(testPeriod.getId())).thenReturn(Optional.of(testPeriod));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

        // When & Then
        JournalPostingException exception = assertThrows(JournalPostingException.class, () -> {
            businessValidationService.validateJournalForPosting(testJournal);
        });

        assertEquals("Account is not active: " + testAccount.getAccountCode(), exception.getMessage());
    }

    @Test
    void validateJournalForPosting_NoManualEntryAllowed_ShouldThrowException() {
        // Given
        testAccount.setAllowManualEntry(false);
        testJournal.setJournalType(JournalHeader.JournalType.MANUAL);
        when(accountingPeriodRepository.findById(testPeriod.getId())).thenReturn(Optional.of(testPeriod));
        when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

        // When & Then
        JournalPostingException exception = assertThrows(JournalPostingException.class, () -> {
            businessValidationService.validateJournalForPosting(testJournal);
        });

        assertEquals("Manual entry not allowed for account: " + testAccount.getAccountCode(), exception.getMessage());
    }

    @Test
    void validateJournalForPosting_OutOfBalance_ShouldThrowException() {
        // Given
        JournalLine creditLine = JournalLine.builder()
            .accountId(UUID.randomUUID())
            .creditAmount(BigDecimal.valueOf(800)) // Different from debit amount
            .build();

        testJournal.setJournalLines(Arrays.asList(
            JournalLine.builder()
                .accountId(testAccount.getId())
                .debitAmount(BigDecimal.valueOf(1000))
                .build(),
            creditLine
        ));

        when(accountingPeriodRepository.findById(testPeriod.getId())).thenReturn(Optional.of(testPeriod));
        when(accountRepository.findById(any())).thenReturn(Optional.of(testAccount));

        // When & Then
        JournalPostingException exception = assertThrows(JournalPostingException.class, () -> {
            businessValidationService.validateJournalForPosting(testJournal);
        });

        assertTrue(exception.getMessage().contains("Journal is out of balance"));
    }

    @Test
    void validateAccountForCreation_ValidAccount_ShouldPass() {
        // Given
        Account validAccount = Account.builder()
            .tenantId("test-tenant")
            .accountCode("1001")
            .accountName("Cash")
            .accountType(Account.AccountType.ASSET)
            .normalBalance(Account.NormalBalance.DEBIT)
            .build();

        when(accountRepository.existsByTenantIdAndAccountCode(eq("test-tenant"), eq("1001")))
            .thenReturn(false);

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> {
            businessValidationService.validateAccountForCreation(validAccount);
        });

        verify(accountRepository).existsByTenantIdAndAccountCode("test-tenant", "1001");
    }

    @Test
    void validateAccountForCreation_EmptyAccountCode_ShouldThrowException() {
        // Given
        Account invalidAccount = Account.builder()
            .tenantId("test-tenant")
            .accountCode("")
            .accountName("Cash")
            .accountType(Account.AccountType.ASSET)
            .normalBalance(Account.NormalBalance.DEBIT)
            .build();

        // When & Then
        AccountValidationException exception = assertThrows(AccountValidationException.class, () -> {
            businessValidationService.validateAccountForCreation(invalidAccount);
        });

        assertEquals("Account code is required", exception.getMessage());
    }

    @Test
    void validateAccountForCreation_InvalidAccountCodeFormat_ShouldThrowException() {
        // Given
        Account invalidAccount = Account.builder()
            .tenantId("test-tenant")
            .accountCode("ABC123") // Contains letters
            .accountName("Cash")
            .accountType(Account.AccountType.ASSET)
            .normalBalance(Account.NormalBalance.DEBIT)
            .build();

        // When & Then
        AccountValidationException exception = assertThrows(AccountValidationException.class, () -> {
            businessValidationService.validateAccountForCreation(invalidAccount);
        });

        assertEquals("Account code must contain only numbers", exception.getMessage());
    }

    @Test
    void validateAccountForCreation_DuplicateAccountCode_ShouldThrowException() {
        // Given
        Account duplicateAccount = Account.builder()
            .tenantId("test-tenant")
            .accountCode("1001")
            .accountName("Cash Duplicate")
            .accountType(Account.AccountType.ASSET)
            .normalBalance(Account.NormalBalance.DEBIT)
            .build();

        when(accountRepository.existsByTenantIdAndAccountCode(eq("test-tenant"), eq("1001")))
            .thenReturn(true);

        // When & Then
        AccountValidationException exception = assertThrows(AccountValidationException.class, () -> {
            businessValidationService.validateAccountForCreation(duplicateAccount);
        });

        assertEquals("Account code already exists: 1001", exception.getMessage());
    }

    @Test
    void validateAccountForCreation_CircularReference_ShouldThrowException() {
        // Given
        UUID accountId = UUID.randomUUID();
        Account accountWithCircularRef = Account.builder()
            .id(accountId)
            .tenantId("test-tenant")
            .accountCode("1002")
            .accountName("Account with circular reference")
            .accountType(Account.AccountType.ASSET)
            .normalBalance(Account.NormalBalance.DEBIT)
            .parentAccountId(accountId) // Self-reference
            .build();

        when(accountRepository.existsByTenantIdAndAccountCode(eq("test-tenant"), eq("1002")))
            .thenReturn(false);

        // When & Then
        AccountValidationException exception = assertThrows(AccountValidationException.class, () -> {
            businessValidationService.validateAccountForCreation(accountWithCircularRef);
        });

        assertEquals("Circular reference detected in account hierarchy", exception.getMessage());
    }

    @Test
    void validateBudgetForCreation_ValidBudget_ShouldPass() {
        // Given
        Budget validBudget = Budget.builder()
            .tenantId("test-tenant")
            .fiscalYearId(testFiscalYear.getId())
            .budgetName("Test Budget")
            .budgetType(Budget.BudgetType.OPERATING)
            .totalBudgetAmount(BigDecimal.valueOf(100000))
            .build();

        when(fiscalYearRepository.findById(testFiscalYear.getId())).thenReturn(Optional.of(testFiscalYear));
        when(budgetLineRepository.findByTenantIdAndAccountId(eq("test-tenant"), any()))
            .thenReturn(Collections.emptyList());

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> {
            businessValidationService.validateBudgetForCreation(validBudget);
        });

        verify(fiscalYearRepository).findById(testFiscalYear.getId());
    }

    @Test
    void validateBudgetForCreation_ClosedFiscalYear_ShouldThrowException() {
        // Given
        testFiscalYear.setIsClosed(true);
        Budget budgetWithClosedFY = Budget.builder()
            .tenantId("test-tenant")
            .fiscalYearId(testFiscalYear.getId())
            .budgetName("Test Budget")
            .build();

        when(fiscalYearRepository.findById(testFiscalYear.getId())).thenReturn(Optional.of(testFiscalYear));

        // When & Then
        BudgetValidationException exception = assertThrows(BudgetValidationException.class, () -> {
            businessValidationService.validateBudgetForCreation(budgetWithClosedFY);
        });

        assertEquals("Cannot create budget for closed fiscal year", exception.getMessage());
    }

    @Test
    void validateYearEndClosing_ValidFiscalYear_ShouldPass() {
        // Given
        when(fiscalYearRepository.findById(testFiscalYear.getId())).thenReturn(Optional.of(testFiscalYear));
        when(accountingPeriodRepository.findOpenPeriodsByFiscalYear(eq("test-tenant"), eq(testFiscalYear.getId())))
            .thenReturn(Collections.emptyList());
        when(journalHeaderRepository.findByTenantIdAndStatus(eq("test-tenant"), eq(JournalHeader.JournalStatus.PENDING_APPROVAL)))
            .thenReturn(Collections.emptyList());

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> {
            businessValidationService.validateYearEndClosing(testFiscalYear.getId());
        });

        verify(fiscalYearRepository).findById(testFiscalYear.getId());
        verify(accountingPeriodRepository).findOpenPeriodsByFiscalYear("test-tenant", testFiscalYear.getId());
    }

    @Test
    void validateYearEndClosing_AlreadyClosedFiscalYear_ShouldThrowException() {
        // Given
        testFiscalYear.setIsClosed(true);
        when(fiscalYearRepository.findById(testFiscalYear.getId())).thenReturn(Optional.of(testFiscalYear));

        // When & Then
        AccountValidationException exception = assertThrows(AccountValidationException.class, () -> {
            businessValidationService.validateYearEndClosing(testFiscalYear.getId());
        });

        assertEquals("Fiscal year is already closed", exception.getMessage());
    }

    @Test
    void validateYearEndClosing_OpenPeriods_ShouldThrowException() {
        // Given
        AccountingPeriod openPeriod = AccountingPeriod.builder()
            .id(UUID.randomUUID())
            .periodName("Open Period")
            .isOpen(true)
            .isClosed(false)
            .build();

        when(fiscalYearRepository.findById(testFiscalYear.getId())).thenReturn(Optional.of(testFiscalYear));
        when(accountingPeriodRepository.findOpenPeriodsByFiscalYear(eq("test-tenant"), eq(testFiscalYear.getId())))
            .thenReturn(Arrays.asList(openPeriod));

        // When & Then
        AccountValidationException exception = assertThrows(AccountValidationException.class, () -> {
            businessValidationService.validateYearEndClosing(testFiscalYear.getId());
        });

        assertEquals("Cannot close fiscal year with open periods: 1", exception.getMessage());
    }

    @Test
    void validateJournalReversal_ValidJournal_ShouldPass() {
        // Given
        JournalHeader postedJournal = JournalHeader.builder()
            .id(UUID.randomUUID())
            .tenantId("test-tenant")
            .status(JournalHeader.JournalStatus.POSTED)
            .isReversed(false)
            .build();

        // When & Then - Should not throw any exception
        assertDoesNotThrow(() -> {
            businessValidationService.validateJournalReversal(postedJournal, "Test reversal reason");
        });
    }

    @Test
    void validateJournalReversal_UnpostedJournal_ShouldThrowException() {
        // Given
        JournalHeader unpostedJournal = JournalHeader.builder()
            .id(UUID.randomUUID())
            .tenantId("test-tenant")
            .status(JournalHeader.JournalStatus.DRAFT)
            .isReversed(false)
            .build();

        // When & Then
        JournalPostingException exception = assertThrows(JournalPostingException.class, () -> {
            businessValidationService.validateJournalReversal(unpostedJournal, "Test reversal reason");
        });

        assertEquals("Only posted journals can be reversed", exception.getMessage());
    }

    @Test
    void validateJournalReversal_AlreadyReversed_ShouldThrowException() {
        // Given
        JournalHeader reversedJournal = JournalHeader.builder()
            .id(UUID.randomUUID())
            .tenantId("test-tenant")
            .status(JournalHeader.JournalStatus.POSTED)
            .isReversed(true)
            .build();

        // When & Then
        JournalPostingException exception = assertThrows(JournalPostingException.class, () -> {
            businessValidationService.validateJournalReversal(reversedJournal, "Test reversal reason");
        });

        assertEquals("Journal has already been reversed", exception.getMessage());
    }

    @Test
    void validateJournalReversal_EmptyReason_ShouldThrowException() {
        // Given
        JournalHeader postedJournal = JournalHeader.builder()
            .id(UUID.randomUUID())
            .tenantId("test-tenant")
            .status(JournalHeader.JournalStatus.POSTED)
            .isReversed(false)
            .build();

        // When & Then
        JournalPostingException exception = assertThrows(JournalPostingException.class, () -> {
            businessValidationService.validateJournalReversal(postedJournal, "");
        });

        assertEquals("Reversal reason is required", exception.getMessage());
    }
}
