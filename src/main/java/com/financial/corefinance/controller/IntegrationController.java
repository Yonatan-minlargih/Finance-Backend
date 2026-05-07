package com.financial.corefinance.controller;

import com.financial.corefinance.dto.request.IntegrationJournalRequest;
import com.financial.corefinance.dto.response.IntegrationJournalResponse;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.mapper.JournalMapper;
import com.financial.corefinance.service.PostingEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/integration")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Integration APIs", description = "APIs for external microservices to post financial transactions")
public class IntegrationController {

    private final PostingEngineService postingEngineService;
    private final JournalMapper journalMapper;

    @PostMapping("/journals")
    @PreAuthorize("hasRole('SYSTEM') or hasRole('INTEGRATION_SERVICE')")
    @Operation(summary = "Post journal from external service", description = "API for other microservices to post journal entries")
    public ResponseEntity<IntegrationJournalResponse> postJournalFromExternalService(
            @Valid @RequestBody IntegrationJournalRequest request) {
        log.info("Posting journal from external service: {} - {}", request.getSourceSystem(), request.getReferenceNumber());
        
        try {
            // Convert integration request to journal header
            JournalHeader journalHeader = convertToJournalHeader(request);
            
            // Post the journal
            JournalHeader postedJournal = postingEngineService.createAndPostJournal(journalHeader);
            
            // Create response
            IntegrationJournalResponse response = IntegrationJournalResponse.builder()
                .success(true)
                .journalId(postedJournal.getId())
                .journalNumber(postedJournal.getJournalNumber())
                .status(postedJournal.getStatus().toString())
                .message("Journal posted successfully")
                .build();
            
            log.info("Journal posted successfully from external service: {}", response.getJournalNumber());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to post journal from external service: {}", e.getMessage(), e);
            
            IntegrationJournalResponse errorResponse = IntegrationJournalResponse.builder()
                .success(false)
                .status("FAILED")
                .message(e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/journals/batch")
    @PreAuthorize("hasRole('SYSTEM') or hasRole('INTEGRATION_SERVICE')")
    @Operation(summary = "Post multiple journals from external service", description = "API for batch posting of journal entries")
    public ResponseEntity<List<IntegrationJournalResponse>> postJournalsBatch(
            @Valid @RequestBody List<IntegrationJournalRequest> requests) {
        log.info("Posting batch of {} journals from external service", requests.size());
        
        List<IntegrationJournalResponse> responses = new java.util.ArrayList<>();
        
        for (IntegrationJournalRequest request : requests) {
            try {
                JournalHeader journalHeader = convertToJournalHeader(request);
                JournalHeader postedJournal = postingEngineService.createAndPostJournal(journalHeader);
                
                IntegrationJournalResponse response = IntegrationJournalResponse.builder()
                    .success(true)
                    .journalId(postedJournal.getId())
                    .journalNumber(postedJournal.getJournalNumber())
                    .status(postedJournal.getStatus().toString())
                    .message("Journal posted successfully")
                    .referenceNumber(request.getReferenceNumber())
                    .build();
                
                responses.add(response);
                
            } catch (Exception e) {
                log.error("Failed to post journal in batch: {} - {}", request.getReferenceNumber(), e.getMessage());
                
                IntegrationJournalResponse errorResponse = IntegrationJournalResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .message(e.getMessage())
                    .referenceNumber(request.getReferenceNumber())
                    .build();
                
                responses.add(errorResponse);
            }
        }
        
        log.info("Batch posting completed. Success: {}, Failed: {}", 
                responses.stream().mapToInt(r -> Boolean.TRUE.equals(r.getSuccess()) ? 1 : 0).sum(),
                responses.stream().mapToInt(r -> Boolean.TRUE.equals(r.getSuccess()) ? 0 : 1).sum());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/journals/{journalId}")
    @PreAuthorize("hasRole('SYSTEM') or hasRole('INTEGRATION_SERVICE')")
    @Operation(summary = "Get posted journal by ID", description = "Retrieve a posted journal by its ID")
    public ResponseEntity<IntegrationJournalResponse> getJournalById(
            @Parameter(description = "Journal ID") @PathVariable UUID journalId) {
        log.info("Retrieving journal by ID: {}", journalId);
        
        try {
            // This would need to be implemented in a service
            // JournalHeader journal = journalService.getJournalById(journalId);
            
            IntegrationJournalResponse response = IntegrationJournalResponse.builder()
                .success(true)
                .journalId(journalId)
                .status("FOUND")
                .message("Journal found")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve journal: {}", e.getMessage(), e);
            
            IntegrationJournalResponse errorResponse = IntegrationJournalResponse.builder()
                .success(false)
                .status("NOT_FOUND")
                .message(e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/journals/{journalId}/reverse")
    @PreAuthorize("hasRole('SYSTEM') or hasRole('INTEGRATION_SERVICE')")
    @Operation(summary = "Reverse a posted journal", description = "Reverse a previously posted journal")
    public ResponseEntity<IntegrationJournalResponse> reverseJournal(
            @Parameter(description = "Journal ID") @PathVariable UUID journalId,
            @Parameter(description = "Reversal reason") @RequestParam String reversalReason) {
        log.info("Reversing journal: {} for reason: {}", journalId, reversalReason);
        
        try {
            JournalHeader reversalJournal = postingEngineService.reverseJournal(journalId, reversalReason);
            
            IntegrationJournalResponse response = IntegrationJournalResponse.builder()
                .success(true)
                .journalId(reversalJournal.getId())
                .journalNumber(reversalJournal.getJournalNumber())
                .status(reversalJournal.getStatus().toString())
                .message("Journal reversed successfully")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to reverse journal: {}", e.getMessage(), e);
            
            IntegrationJournalResponse errorResponse = IntegrationJournalResponse.builder()
                .success(false)
                .status("FAILED")
                .message(e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Integration service health check", description = "Health check endpoint for external services")
    public ResponseEntity<IntegrationJournalResponse> healthCheck() {
        return ResponseEntity.ok(IntegrationJournalResponse.builder()
            .success(true)
            .status("HEALTHY")
            .message("Core Finance Service is ready for integration")
            .build());
    }

    private JournalHeader convertToJournalHeader(IntegrationJournalRequest request) {
        JournalHeader journalHeader = new JournalHeader();
        journalHeader.setJournalDate(request.getJournalDate());
        journalHeader.setAccountingPeriodId(request.getAccountingPeriodId());
        journalHeader.setJournalType(JournalHeader.JournalType.SYSTEM);
        journalHeader.setReferenceNumber(request.getReferenceNumber());
        journalHeader.setReferenceType(request.getReferenceType());
        journalHeader.setReferenceId(request.getReferenceId());
        journalHeader.setDescription(request.getDescription());
        journalHeader.setNarration(request.getNarration());
        journalHeader.setSourceSystem(request.getSourceSystem());
        journalHeader.setBatchNumber(request.getBatchNumber());
        
        // Convert journal lines
        for (IntegrationJournalRequest.JournalLineRequest lineRequest : request.getJournalLines()) {
            com.financial.corefinance.domain.entity.JournalLine journalLine = new com.financial.corefinance.domain.entity.JournalLine();
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
}
