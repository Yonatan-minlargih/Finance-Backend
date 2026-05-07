package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.*;
import com.financial.corefinance.exception.JournalPostingException;
import com.financial.corefinance.repository.*;
import com.financial.corefinance.service.validation.BusinessValidationService;
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
class PostingEngineServiceTest {

    @Mock
    private JournalHeaderRepository journalHeaderRepository;
    
    @Mock
    private JournalLineRepository journalLineRepository;
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private AccountingPeriodRepository accountingPeriodRepository;
    
    @Mock
    private NumberingSeriesRepository numberingSeriesRepository;
    
    @Mock
    private AuditLogRepository auditLogRepository;
    
    @Mock
    private BusinessValidationService businessValidationService;
    
    @Mock
    private ApprovalWorkflowRepository approvalWorkflowRepository;

    @InjectMocks
    private PostingEngineService postingEngineService;

    private JournalHeader testJournal;
    private Account testAccount;
    private AccountingPeriod testPeriod;
    private NumberingSeries testNumberingSeries;

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

        testNumberingSeries = NumberingSeries.builder()
            .id(UUID.randomUUID())
            .tenantId("test-tenant")
            .seriesCode("JRN")
            .seriesName("Journal Entries")
            .prefix("JRN-")
            .currentNumber(1000)
            .numberLength(6)
            .build();

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
    void createAndPostJournal_ValidJournal_ShouldPostSuccessfully() {
        // Given
        when(businessValidationService.validateJournalForPosting(any())).thenAnswer(invocation -> null);
        when(numberingSeriesRepository.findByTenantIdAndSeriesCode(eq("test-tenant"), eq("JRN")))
            .thenReturn(Optional.of(testNumberingSeries));
        when(journalHeaderRepository.save(any(JournalHeader.class))).thenAnswer(invocation -> {
            JournalHeader saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setJournalNumber("JRN-001001");
            return saved;
        });
        when(journalLineRepository.saveAll(any())).thenReturn(Collections.emptyList());

        // When
        JournalHeader result = postingEngineService.createAndPostJournal(testJournal);

        // Then
        assertNotNull(result);
        assertEquals("JRN-001001", result.getJournalNumber());
        assertEquals(JournalHeader.JournalStatus.POSTED, result.getStatus());
        assertNotNull(result.getPostedAt());
        verify(businessValidationService).validateJournalForPosting(testJournal);
        verify(journalHeaderRepository).save(testJournal);
        verify(journalLineRepository).saveAll(any());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void createAndPostJournal_InvalidJournal_ShouldThrowException() {
        // Given
        doThrow(new JournalPostingException("Journal is out of balance"))
            .when(businessValidationService).validateJournalForPosting(any());

        // When & Then
        assertThrows(JournalPostingException.class, () -> {
            postingEngineService.createAndPostJournal(testJournal);
        });

        verify(businessValidationService).validateJournalForPosting(testJournal);
        verify(journalHeaderRepository, never()).save(any());
    }

    @Test
    void saveDraftJournal_ValidJournal_ShouldSaveAsDraft() {
        // Given
        when(journalHeaderRepository.save(any(JournalHeader.class))).thenAnswer(invocation -> {
            JournalHeader saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // When
        JournalHeader result = postingEngineService.saveDraftJournal(testJournal);

        // Then
        assertNotNull(result);
        assertEquals(JournalHeader.JournalStatus.DRAFT, result.getStatus());
        verify(journalHeaderRepository).save(testJournal);
        verify(businessValidationService, never()).validateJournalForPosting(any());
    }

    @Test
    void postJournal_ValidDraftJournal_ShouldPostSuccessfully() {
        // Given
        UUID journalId = UUID.randomUUID();
        JournalHeader draftJournal = JournalHeader.builder()
            .id(journalId)
            .tenantId("test-tenant")
            .status(JournalHeader.JournalStatus.DRAFT)
            .build();

        when(journalHeaderRepository.findById(journalId)).thenReturn(Optional.of(draftJournal));
        when(businessValidationService.validateJournalForPosting(any())).thenAnswer(invocation -> null);
        when(numberingSeriesRepository.findByTenantIdAndSeriesCode(eq("test-tenant"), eq("JRN")))
            .thenReturn(Optional.of(testNumberingSeries));
        when(journalHeaderRepository.save(any(JournalHeader.class))).thenReturn(draftJournal);

        // When
        JournalHeader result = postingEngineService.postJournal(journalId);

        // Then
        assertNotNull(result);
        assertEquals(JournalHeader.JournalStatus.POSTED, result.getStatus());
        verify(journalHeaderRepository).findById(journalId);
        verify(businessValidationService).validateJournalForPosting(draftJournal);
    }

    @Test
    void postJournal_NonExistentJournal_ShouldThrowException() {
        // Given
        UUID journalId = UUID.randomUUID();
        when(journalHeaderRepository.findById(journalId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(JournalPostingException.class, () -> {
            postingEngineService.postJournal(journalId);
        });

        verify(journalHeaderRepository).findById(journalId);
    }

    @Test
    void reverseJournal_ValidPostedJournal_ShouldCreateReversal() {
        // Given
        UUID originalJournalId = UUID.randomUUID();
        JournalHeader originalJournal = JournalHeader.builder()
            .id(originalJournalId)
            .tenantId("test-tenant")
            .journalNumber("JRN-001001")
            .status(JournalHeader.JournalStatus.POSTED)
            .isReversed(false)
            .build();

        JournalLine originalLine = JournalLine.builder()
            .accountId(testAccount.getId())
            .debitAmount(BigDecimal.valueOf(1000))
            .build();

        originalJournal.setJournalLines(Arrays.asList(originalLine));

        when(journalHeaderRepository.findById(originalJournalId)).thenReturn(Optional.of(originalJournal));
        when(businessValidationService.validateJournalReversal(eq(originalJournal), any()))
            .thenAnswer(invocation -> null);
        when(numberingSeriesRepository.findByTenantIdAndSeriesCode(eq("test-tenant"), eq("JRN")))
            .thenReturn(Optional.of(testNumberingSeries));
        when(journalHeaderRepository.save(any(JournalHeader.class))).thenAnswer(invocation -> {
            JournalHeader saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setJournalNumber("JRN-001002");
            return saved;
        });

        // When
        JournalHeader result = postingEngineService.reverseJournal(originalJournalId, "Test reversal");

        // Then
        assertNotNull(result);
        assertEquals("JRN-001002", result.getJournalNumber());
        assertEquals(JournalHeader.JournalType.REVERSAL, result.getJournalType());
        assertEquals(originalJournalId, result.getOriginalJournalId());
        assertTrue(result.getIsReversal());
        verify(businessValidationService).validateJournalReversal(originalJournal, "Test reversal");
        verify(journalHeaderRepository).save(any(JournalHeader.class));
    }

    @Test
    void reverseJournal_AlreadyReversedJournal_ShouldThrowException() {
        // Given
        UUID originalJournalId = UUID.randomUUID();
        JournalHeader originalJournal = JournalHeader.builder()
            .id(originalJournalId)
            .tenantId("test-tenant")
            .status(JournalHeader.JournalStatus.POSTED)
            .isReversed(true)
            .build();

        when(journalHeaderRepository.findById(originalJournalId)).thenReturn(Optional.of(originalJournal));

        // When & Then
        assertThrows(JournalPostingException.class, () -> {
            postingEngineService.reverseJournal(originalJournalId, "Test reversal");
        });

        verify(journalHeaderRepository).findById(originalJournalId);
        verify(businessValidationService, never()).validateJournalReversal(any(), any());
    }

    @Test
    void validateJournalBalance_BalancedJournal_ShouldReturnTrue() {
        // Given
        JournalHeader balancedJournal = JournalHeader.builder()
            .tenantId("test-tenant")
            .build();

        JournalLine debitLine = JournalLine.builder()
            .accountId(UUID.randomUUID())
            .debitAmount(BigDecimal.valueOf(1000))
            .build();

        JournalLine creditLine = JournalLine.builder()
            .accountId(UUID.randomUUID())
            .creditAmount(BigDecimal.valueOf(1000))
            .build();

        balancedJournal.setJournalLines(Arrays.asList(debitLine, creditLine));

        when(journalHeaderRepository.findById(any())).thenReturn(Optional.of(balancedJournal));

        // When
        boolean result = postingEngineService.validateJournalBalance(UUID.randomUUID());

        // Then
        assertTrue(result);
    }

    @Test
    void validateJournalBalance_UnbalancedJournal_ShouldReturnFalse() {
        // Given
        JournalHeader unbalancedJournal = JournalHeader.builder()
            .tenantId("test-tenant")
            .build();

        JournalLine debitLine = JournalLine.builder()
            .accountId(UUID.randomUUID())
            .debitAmount(BigDecimal.valueOf(1000))
            .build();

        JournalLine creditLine = JournalLine.builder()
            .accountId(UUID.randomUUID())
            .creditAmount(BigDecimal.valueOf(800))
            .build();

        unbalancedJournal.setJournalLines(Arrays.asList(debitLine, creditLine));

        when(journalHeaderRepository.findById(any())).thenReturn(Optional.of(unbalancedJournal));

        // When
        boolean result = postingEngineService.validateJournalBalance(UUID.randomUUID());

        // Then
        assertFalse(result);
    }

    @Test
    void generateJournalNumber_ExistingSeries_ShouldGenerateNextNumber() {
        // Given
        when(numberingSeriesRepository.findByTenantIdAndSeriesCode(eq("test-tenant"), eq("JRN")))
            .thenReturn(Optional.of(testNumberingSeries));

        // When
        postingEngineService.generateJournalNumber(testJournal);

        // Then
        assertEquals("JRN-001001", testJournal.getJournalNumber());
        verify(numberingSeriesRepository).findByTenantIdAndSeriesCode("test-tenant", "JRN");
        verify(numberingSeriesRepository).save(testNumberingSeries);
        assertEquals(1001, testNumberingSeries.getCurrentNumber());
    }

    @Test
    void generateJournalNumber_NewSeries_ShouldCreateAndUseNewSeries() {
        // Given
        when(numberingSeriesRepository.findByTenantIdAndSeriesCode(eq("test-tenant"), eq("JRN")))
            .thenReturn(Optional.empty());
        when(numberingSeriesRepository.save(any(NumberingSeries.class))).thenAnswer(invocation -> {
            NumberingSeries saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // When
        postingEngineService.generateJournalNumber(testJournal);

        // Then
        assertNotNull(testJournal.getJournalNumber());
        assertTrue(testJournal.getJournalNumber().startsWith("JRN-"));
        verify(numberingSeriesRepository).findByTenantIdAndSeriesCode("test-tenant", "JRN");
        verify(numberingSeriesRepository).save(any(NumberingSeries.class));
    }

    @Test
    void getJournalsForPosting_ShouldReturnApprovedJournals() {
        // Given
        String tenantId = "test-tenant";
        List<JournalHeader> expectedJournals = Arrays.asList(testJournal);
        when(journalHeaderRepository.findJournalsForPosting(eq(tenantId), 
                eq(JournalHeader.JournalStatus.APPROVED), any(LocalDate.class)))
            .thenReturn(expectedJournals);

        // When
        List<JournalHeader> result = postingEngineService.getJournalsForPosting(tenantId);

        // Then
        assertEquals(expectedJournals, result);
        verify(journalHeaderRepository).findJournalsForPosting(tenantId, 
                JournalHeader.JournalStatus.APPROVED, LocalDate.now());
    }
}
