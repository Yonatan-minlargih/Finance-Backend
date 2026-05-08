package com.financial.corefinance.service;

import com.financial.corefinance.domain.base.TenantContext;
import com.financial.corefinance.domain.entity.*;
import com.financial.corefinance.repository.*;
import com.financial.corefinance.service.validation.BusinessValidationService;
import com.financial.corefinance.event.FinanceEventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class PostingEngineService {

    private final JournalHeaderRepository journalHeaderRepository;
    private final JournalLineRepository journalLineRepository;
    private final AccountRepository accountRepository;
    private final AccountingPeriodRepository accountingPeriodRepository;
    private final NumberingSeriesRepository numberingSeriesRepository;
    private final AuditLogRepository auditLogRepository;
    private final BusinessValidationService businessValidationService;
    private final ApprovalWorkflowRepository approvalWorkflowRepository;
    private final FinanceEventService financeEventService;

    @Transactional
    public JournalHeader createAndPostJournal(@Valid @NotNull JournalHeader journalHeader) {
        log.info("Creating and posting journal: {}", journalHeader.getDescription());
        
        hydrateDefaults(journalHeader);
        businessValidationService.validateJournalForPosting(journalHeader);

        if (journalHeader.getJournalNumber() == null || journalHeader.getJournalNumber().isBlank()) {
            generateJournalNumber(journalHeader);
        }

        validateAccountingPeriod(journalHeader);
        validateAccounts(journalHeader);
        validateJournalBalance(journalHeader);

        setPostingDetails(journalHeader);

        JournalHeader savedJournal = journalHeaderRepository.save(journalHeader);

        createAuditLog(savedJournal, AuditLog.AuditAction.POST, "Journal posted successfully");
        financeEventService.publishJournalPostedEvent(savedJournal.getId(), savedJournal.getTenantId(), savedJournal.getCreatedBy());

        log.info("Journal posted successfully: {}", savedJournal.getJournalNumber());
        return savedJournal;
    }

    @Transactional
    public JournalHeader saveDraftJournal(@Valid @NotNull JournalHeader journalHeader) {
        log.info("Saving draft journal: {}", journalHeader.getDescription());
        
        hydrateDefaults(journalHeader);
        if (journalHeader.getJournalNumber() == null || journalHeader.getJournalNumber().isEmpty()) {
            generateJournalNumber(journalHeader);
        }

        validateJournalForDraft(journalHeader);
        journalHeader.setStatus(JournalHeader.JournalStatus.DRAFT);
        linkJournalLines(journalHeader);
        JournalHeader savedJournal = journalHeaderRepository.save(journalHeader);
        createAuditLog(savedJournal, AuditLog.AuditAction.CREATE, "Journal saved as draft");

        log.info("Draft journal saved with number: {}", savedJournal.getJournalNumber());
        return savedJournal;
    }

    @Transactional
    public JournalHeader postJournal(@NotNull UUID journalId) {
        log.info("Posting journal with ID: {}", journalId);
        
        JournalHeader journal = journalHeaderRepository.findById(journalId)
            .orElseThrow(() -> new IllegalArgumentException("Journal not found: " + journalId));
        
        if (journal.getStatus() != JournalHeader.JournalStatus.APPROVED
                && journal.getStatus() != JournalHeader.JournalStatus.DRAFT) {
            throw new IllegalStateException("Only approved or draft journals can be posted. Current status: " + journal.getStatus());
        }

        return createAndPostJournal(journal);
    }

    @Transactional
    public JournalHeader reverseJournal(@NotNull UUID originalJournalId, @NotNull String reversalReason) {
        log.info("Reversing journal with ID: {}", originalJournalId);
        
        JournalHeader originalJournal = journalHeaderRepository.findById(originalJournalId)
            .orElseThrow(() -> new IllegalArgumentException("Original journal not found: " + originalJournalId));
        
        if (originalJournal.getStatus() != JournalHeader.JournalStatus.POSTED) {
            throw new IllegalStateException("Only posted journals can be reversed. Current status: " + originalJournal.getStatus());
        }
        
        if (originalJournal.getIsReversed()) {
            throw new IllegalStateException("Journal has already been reversed");
        }
        
        JournalHeader reversalJournal = createReversalJournal(originalJournal, reversalReason);
        JournalHeader postedReversal = createAndPostJournal(reversalJournal);

        originalJournal.setIsReversed(true);
        originalJournal.setStatus(JournalHeader.JournalStatus.REVERSED);
        originalJournal.setReversedBy(postedReversal.getCreatedBy());
        originalJournal.setReversedAt(LocalDateTime.now());
        originalJournal.setReversalReason(reversalReason);
        journalHeaderRepository.save(originalJournal);

        createAuditLog(originalJournal, AuditLog.AuditAction.REVERSE, "Journal reversed: " + reversalReason);

        log.info("Journal reversed successfully. Reversal journal number: {}", postedReversal.getJournalNumber());
        return postedReversal;
    }

    @Transactional(readOnly = true)
    public JournalHeader getJournalById(@NotNull UUID journalId) {
        return journalHeaderRepository.findById(journalId)
                .orElseThrow(() -> new IllegalArgumentException("Journal not found: " + journalId));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<JournalHeader> getAllJournals(org.springframework.data.domain.Pageable pageable) {
        return journalHeaderRepository.findByTenantId(currentTenant(), pageable);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<JournalHeader> searchJournals(String search, org.springframework.data.domain.Pageable pageable) {
        return journalHeaderRepository.searchJournals(currentTenant(), search, pageable);
    }

    @Transactional(readOnly = true)
    public List<JournalHeader> getJournalsByStatus(JournalHeader.JournalStatus status) {
        return journalHeaderRepository.findByTenantIdAndStatus(currentTenant(), status);
    }

    @Transactional(readOnly = true)
    public List<JournalLine> getUnreconciledPostedLines() {
        return journalLineRepository.findUnreconciledPostedLines(currentTenant());
    }

    @Transactional
    public void markJournalLineReconciled(@NotNull UUID journalLineId, boolean reconciled) {
        JournalLine line = journalLineRepository.findById(journalLineId)
                .orElseThrow(() -> new IllegalArgumentException("Journal line not found: " + journalLineId));
        line.setReconciled(reconciled);
        journalLineRepository.save(line);
    }

    private void validateJournalForPosting(JournalHeader journalHeader) {
        if (journalHeader.getJournalLines() == null || journalHeader.getJournalLines().isEmpty()) {
            throw new IllegalArgumentException("Journal must have at least one line");
        }
        
        if (journalHeader.getJournalDate() == null) {
            throw new IllegalArgumentException("Journal date is required");
        }
        
        if (journalHeader.getAccountingPeriodId() == null) {
            throw new IllegalArgumentException("Accounting period is required");
        }
        
        if (journalHeader.getDescription() == null || journalHeader.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Journal description is required");
        }
    }

    private void validateJournalForDraft(JournalHeader journalHeader) {
        if (journalHeader.getJournalDate() == null) {
            throw new IllegalArgumentException("Journal date is required");
        }
        if (journalHeader.getAccountingPeriodId() == null) {
            throw new IllegalArgumentException("Accounting period is required");
        }
        if (journalHeader.getDescription() == null || journalHeader.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Journal description is required");
        }
    }

    private void hydrateDefaults(JournalHeader journalHeader) {
        if (journalHeader.getTenantId() == null || journalHeader.getTenantId().isBlank()) {
            journalHeader.setTenantId(currentTenant());
        }
        if (journalHeader.getJournalLines() == null) {
            journalHeader.setJournalLines(new ArrayList<>());
        }
        if (journalHeader.getJournalType() == null) {
            journalHeader.setJournalType(JournalHeader.JournalType.MANUAL);
        }
    }

    private void generateJournalNumber(JournalHeader journalHeader) {
        String tenantId = journalHeader.getTenantId();
        String seriesCode = "JOURNAL";
        
        NumberingSeries numberingSeries = numberingSeriesRepository
            .findByTenantIdAndSeriesCodeForUpdate(tenantId, seriesCode)
            .orElseThrow(() -> new IllegalArgumentException("Journal numbering series not configured"));
        
        if (!numberingSeries.getIsActive()) {
            throw new IllegalArgumentException("Journal numbering series is not active");
        }
        
        String journalNumber = numberingSeries.generateNextNumber();
        journalHeader.setJournalNumber(journalNumber);
        
        // Increment the number
        numberingSeries.incrementNumber();
        numberingSeriesRepository.save(numberingSeries);
    }

    private void validateAccountingPeriod(JournalHeader journalHeader) {
        AccountingPeriod period = accountingPeriodRepository.findById(journalHeader.getAccountingPeriodId())
            .orElseThrow(() -> new IllegalArgumentException("Accounting period not found"));

        if (!period.getTenantId().equals(journalHeader.getTenantId())) {
            throw new IllegalArgumentException("Accounting period does not belong to tenant");
        }
        if (!period.getIsOpen() || period.getIsClosed()) {
            throw new IllegalArgumentException("Accounting period is closed: " + period.getPeriodName());
        }

        LocalDate journalDate = journalHeader.getJournalDate();
        if (journalDate.isBefore(period.getStartDate()) || journalDate.isAfter(period.getEndDate())) {
            throw new IllegalArgumentException("Journal date must be inside accounting period range");
        }
    }

    private void validateAccounts(JournalHeader journalHeader) {
        for (JournalLine line : journalHeader.getJournalLines()) {
            Account account = accountRepository.findById(line.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + line.getAccountId()));

            if (!account.getTenantId().equals(journalHeader.getTenantId())) {
                throw new IllegalArgumentException("Account does not belong to tenant: " + account.getAccountCode());
            }
            if (!account.getIsActive()) {
                throw new IllegalArgumentException("Account is not active: " + account.getAccountCode());
            }

            if (!account.getAllowManualEntry()) {
                throw new IllegalArgumentException("Manual entry not allowed for account: " + account.getAccountCode());
            }

            BigDecimal debit = line.getDebitAmount() == null ? BigDecimal.ZERO : line.getDebitAmount();
            BigDecimal credit = line.getCreditAmount() == null ? BigDecimal.ZERO : line.getCreditAmount();
            if (debit.compareTo(BigDecimal.ZERO) > 0 && credit.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException("Journal line cannot have both debit and credit values");
            }
            if (debit.compareTo(BigDecimal.ZERO) == 0 && credit.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("Journal line must have debit or credit amount");
            }
            if (debit.compareTo(BigDecimal.ZERO) < 0 || credit.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Journal line amounts cannot be negative");
            }
        }
    }

    private void validateJournalBalance(JournalHeader journalHeader) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (JournalLine line : journalHeader.getJournalLines()) {
            if (line.getDebitAmount() != null && line.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalDebit = totalDebit.add(line.getDebitAmount());
            }
            if (line.getCreditAmount() != null && line.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalCredit = totalCredit.add(line.getCreditAmount());
            }
        }
        
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalArgumentException(
                String.format("Journal is out of balance. Total Debit: %s, Total Credit: %s", totalDebit, totalCredit));
        }

        journalHeader.setTotalDebit(totalDebit);
        journalHeader.setTotalCredit(totalCredit);
    }

    private void setPostingDetails(JournalHeader journalHeader) {
        journalHeader.setStatus(JournalHeader.JournalStatus.POSTED);
        journalHeader.setPostedAt(LocalDateTime.now());
        journalHeader.setPostedBy(journalHeader.getCreatedBy()); // Will be set by auditor
        linkJournalLines(journalHeader);
    }

    private void linkJournalLines(JournalHeader journalHeader) {
        int lineNumber = 1;
        for (JournalLine line : journalHeader.getJournalLines()) {
            line.setTenantId(journalHeader.getTenantId());
            line.setLineNumber(lineNumber++);
            line.setJournalHeader(journalHeader);
            line.setJournalHeaderId(journalHeader.getId());
        }
    }

    private JournalHeader createReversalJournal(JournalHeader originalJournal, String reversalReason) {
        JournalHeader reversalJournal = new JournalHeader();
        reversalJournal.setTenantId(originalJournal.getTenantId());
        reversalJournal.setJournalDate(LocalDateTime.now().toLocalDate());
        reversalJournal.setAccountingPeriodId(originalJournal.getAccountingPeriodId());
        reversalJournal.setJournalType(JournalHeader.JournalType.REVERSAL);
        reversalJournal.setDescription("Reversal of " + originalJournal.getJournalNumber() + " - " + reversalReason);
        reversalJournal.setNarration("Reversal of journal: " + originalJournal.getJournalNumber());
        reversalJournal.setOriginalJournalId(originalJournal.getId());
        reversalJournal.setIsReversed(false);
        
        // Create reversal lines
        for (JournalLine originalLine : originalJournal.getJournalLines()) {
            JournalLine reversalLine = new JournalLine();
            reversalLine.setTenantId(originalLine.getTenantId());
            reversalLine.setAccountId(originalLine.getAccountId());
            reversalLine.setDescription("Reversal: " + originalLine.getDescription());
            
            // Swap debit and credit
            if (originalLine.getDebitAmount() != null && originalLine.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                reversalLine.setCreditAmount(originalLine.getDebitAmount());
            }
            if (originalLine.getCreditAmount() != null && originalLine.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                reversalLine.setDebitAmount(originalLine.getCreditAmount());
            }
            
            reversalJournal.getJournalLines().add(reversalLine);
        }
        
        return reversalJournal;
    }

    private void createAuditLog(JournalHeader journal, AuditLog.AuditAction action, String description) {
        AuditLog auditLog = AuditLog.builder()
            .tenantId(journal.getTenantId())
            .entityType("JournalHeader")
            .entityId(journal.getId())
            .action(action)
            .newValue("Journal Number: " + journal.getJournalNumber() + ", Status: " + journal.getStatus())
            .userId(journal.getCreatedBy())
            .additionalInfo(description)
            .moduleName("PostingEngine")
            .functionName("createAndPostJournal")
            .businessTransactionId(journal.getJournalNumber())
            .build();
        
        auditLogRepository.save(auditLog);
    }

    public List<JournalHeader> getJournalsForPosting(String tenantId) {
        return journalHeaderRepository.findJournalsForPosting(
            tenantId, JournalHeader.JournalStatus.APPROVED, LocalDateTime.now().toLocalDate());
    }

    public boolean validateJournalBalance(UUID journalId) {
        JournalHeader journal = journalHeaderRepository.findById(journalId)
            .orElseThrow(() -> new IllegalArgumentException("Journal not found: " + journalId));
        
        try {
            validateJournalBalance(journal);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String currentTenant() {
        String tenant = TenantContext.getCurrentTenant();
        if (tenant == null || tenant.isBlank()) {
            throw new IllegalStateException("Tenant context is required");
        }
        return tenant;
    }
}
