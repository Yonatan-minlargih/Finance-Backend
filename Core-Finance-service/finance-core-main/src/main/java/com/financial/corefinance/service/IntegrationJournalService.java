package com.financial.corefinance.service;

import com.financial.corefinance.domain.entity.IntegrationIngressLog;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.dto.request.IntegrationJournalRequest;
import com.financial.corefinance.repository.IntegrationIngressLogRepository;
import jakarta.transaction.Transactional;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntegrationJournalService {

    private static final Set<String> ALLOWED_SOURCE_SYSTEMS = Set.of(
            "CORE_FINANCE",
            "TRANSACTIONAL_SERVICE",
            "COSTING_SERVICE",
            "PAYROLL_SERVICE"
    );

    private final PostingEngineService postingEngineService;
    private final IntegrationIngressLogRepository ingressLogRepository;

    @Transactional
    public JournalHeader postJournal(IntegrationJournalRequest request) {
        String tenantId = request.getTenantId();
        String idempotencyKey = normalize(request.getIdempotencyKey());
        String eventId = normalize(request.getEventId());

        Optional<IntegrationIngressLog> existing = ingressLogRepository.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey);
        if (existing.isPresent() && existing.get().getJournalId() != null) {
            return postingEngineService.getJournalById(existing.get().getJournalId());
        }

        IntegrationIngressLog logEntry = existing.orElseGet(() -> IntegrationIngressLog.builder()
                .tenantId(tenantId)
                .idempotencyKey(idempotencyKey)
                .eventId(eventId)
                .contractVersion(normalize(request.getContractVersion()))
                .sourceSystem(validateAndNormalizeSourceSystem(request.getSourceSystem()))
                .correlationId(request.getCorrelationId())
                .status(IntegrationIngressLog.IngressStatus.ACCEPTED)
                .build());
        ingressLogRepository.save(logEntry);

        try {
            JournalHeader posted = postingEngineService.createAndPostJournal(convertToJournalHeader(request));
            logEntry.setStatus(IntegrationIngressLog.IngressStatus.PROCESSED);
            logEntry.setJournalId(posted.getId());
            ingressLogRepository.save(logEntry);
            return posted;
        } catch (RuntimeException ex) {
            logEntry.setStatus(IntegrationIngressLog.IngressStatus.FAILED);
            logEntry.setErrorMessage(ex.getMessage());
            ingressLogRepository.save(logEntry);
            throw ex;
        }
    }

    private JournalHeader convertToJournalHeader(IntegrationJournalRequest request) {
        JournalHeader journalHeader = new JournalHeader();
        journalHeader.setTenantId(request.getTenantId());
        journalHeader.setJournalDate(request.getJournalDate());
        journalHeader.setAccountingPeriodId(request.getAccountingPeriodId());
        journalHeader.setJournalType(JournalHeader.JournalType.SYSTEM);
        journalHeader.setReferenceNumber(request.getReferenceNumber());
        journalHeader.setReferenceType(request.getReferenceType());
        journalHeader.setReferenceId(request.getReferenceId());
        journalHeader.setDescription(request.getDescription());
        journalHeader.setNarration(request.getNarration());
        journalHeader.setSourceSystem(validateAndNormalizeSourceSystem(request.getSourceSystem()));
        journalHeader.setBatchNumber(request.getBatchNumber());

        for (IntegrationJournalRequest.JournalLineRequest lineRequest : request.getJournalLines()) {
            com.financial.corefinance.domain.entity.JournalLine journalLine = new com.financial.corefinance.domain.entity.JournalLine();
            journalLine.setTenantId(request.getTenantId());
            journalLine.setAccountId(lineRequest.getAccountId());
            journalLine.setDebitAmount(lineRequest.getDebitAmount());
            journalLine.setCreditAmount(lineRequest.getCreditAmount());
            journalLine.setDescription(lineRequest.getDescription());
            journalLine.setCostCenterId(lineRequest.getCostCenterId());
            journalLine.setDepartmentId(lineRequest.getDepartmentId());
            journalLine.setProjectId(lineRequest.getProjectId());
            journalLine.setAnalysisCode(lineRequest.getAnalysisCode());
            journalLine.setCurrencyCode(lineRequest.getCurrencyCode());
            journalLine.setExchangeRate(lineRequest.getExchangeRate());
            journalHeader.getJournalLines().add(journalLine);
        }

        return journalHeader;
    }

    private String validateAndNormalizeSourceSystem(String sourceSystem) {
        String normalized = normalize(sourceSystem);
        if (!ALLOWED_SOURCE_SYSTEMS.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Unsupported source system. Allowed values: " + String.join(", ", ALLOWED_SOURCE_SYSTEMS)
            );
        }
        return normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
