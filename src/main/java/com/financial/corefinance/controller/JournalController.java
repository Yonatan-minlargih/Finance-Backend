package com.financial.corefinance.controller;

import com.financial.corefinance.dto.request.JournalHeaderRequest;
import com.financial.corefinance.dto.response.JournalHeaderResponse;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.mapper.JournalMapper;
import com.financial.corefinance.service.PostingEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/journals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Journal Management", description = "APIs for managing journal entries and posting")
public class JournalController {

    private final PostingEngineService postingEngineService;
    private final JournalMapper journalMapper;

    @PostMapping
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Create and post a journal entry", description = "Creates a new journal entry and posts it to the general ledger")
    public ResponseEntity<JournalHeaderResponse> createAndPostJournal(
            @Valid @RequestBody JournalHeaderRequest request) {
        log.info("Creating and posting journal: {}", request.getDescription());
        
        JournalHeader journalHeader = journalMapper.toJournalHeader(request);
        JournalHeader postedJournal = postingEngineService.createAndPostJournal(journalHeader);
        
        JournalHeaderResponse response = journalMapper.toJournalHeaderResponse(postedJournal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/draft")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Save journal as draft", description = "Saves a journal entry as draft without posting")
    public ResponseEntity<JournalHeaderResponse> saveDraftJournal(
            @Valid @RequestBody JournalHeaderRequest request) {
        log.info("Saving draft journal: {}", request.getDescription());
        
        JournalHeader journalHeader = journalMapper.toJournalHeader(request);
        JournalHeader savedJournal = postingEngineService.saveDraftJournal(journalHeader);
        
        JournalHeaderResponse response = journalMapper.toJournalHeaderResponse(savedJournal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{journalId}/post")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Post a draft journal", description = "Posts a previously saved draft journal")
    public ResponseEntity<JournalHeaderResponse> postJournal(
            @Parameter(description = "Journal ID") @PathVariable UUID journalId) {
        log.info("Posting journal with ID: {}", journalId);
        
        JournalHeader postedJournal = postingEngineService.postJournal(journalId);
        JournalHeaderResponse response = journalMapper.toJournalHeaderResponse(postedJournal);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{journalId}/reverse")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Reverse a posted journal", description = "Creates and posts a reversal journal for the specified journal")
    public ResponseEntity<JournalHeaderResponse> reverseJournal(
            @Parameter(description = "Journal ID") @PathVariable UUID journalId,
            @Parameter(description = "Reversal reason") @RequestParam String reversalReason) {
        log.info("Reversing journal with ID: {} for reason: {}", journalId, reversalReason);
        
        JournalHeader reversalJournal = postingEngineService.reverseJournal(journalId, reversalReason);
        JournalHeaderResponse response = journalMapper.toJournalHeaderResponse(reversalJournal);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{journalId}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get journal by ID", description = "Retrieves a journal entry by its ID")
    public ResponseEntity<JournalHeaderResponse> getJournal(
            @Parameter(description = "Journal ID") @PathVariable UUID journalId) {
        log.info("Retrieving journal with ID: {}", journalId);
        
        // This would need to be implemented in a service
        // JournalHeader journal = journalService.getJournalById(journalId);
        // JournalHeaderResponse response = journalMapper.toJournalHeaderResponse(journal);
        
        return ResponseEntity.ok().build(); // Placeholder
    }

    @GetMapping
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get all journals", description = "Retrieves a paginated list of journal entries")
    public ResponseEntity<Page<JournalHeaderResponse>> getAllJournals(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        // This would need to be implemented in a service
        // Page<JournalHeader> journals = journalService.getAllJournals(pageable);
        // Page<JournalHeaderResponse> response = journals.map(journalMapper::toJournalHeaderResponse);
        
        return ResponseEntity.ok().build(); // Placeholder
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Search journals", description = "Searches journal entries by various criteria")
    public ResponseEntity<Page<JournalHeaderResponse>> searchJournals(
            @Parameter(description = "Search term") @RequestParam String search,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // This would need to be implemented in a service
        // Page<JournalHeader> journals = journalService.searchJournals(search, pageable);
        // Page<JournalHeaderResponse> response = journals.map(journalMapper::toJournalHeaderResponse);
        
        return ResponseEntity.ok().build(); // Placeholder
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER') or hasRole('AUDITOR')")
    @Operation(summary = "Get journals by status", description = "Retrieves journal entries filtered by status")
    public ResponseEntity<List<JournalHeaderResponse>> getJournalsByStatus(
            @Parameter(description = "Journal status") @PathVariable JournalHeader.JournalStatus status) {
        
        // This would need to be implemented in a service
        // List<JournalHeader> journals = journalService.getJournalsByStatus(status);
        // List<JournalHeaderResponse> response = journals.stream()
        //     .map(journalMapper::toJournalHeaderResponse)
        //     .collect(Collectors.toList());
        
        return ResponseEntity.ok().build(); // Placeholder
    }

    @PostMapping("/{journalId}/validate")
    @PreAuthorize("hasRole('ACCOUNTANT') or hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Validate journal balance", description = "Validates if a journal is balanced")
    public ResponseEntity<Boolean> validateJournalBalance(
            @Parameter(description = "Journal ID") @PathVariable UUID journalId) {
        
        boolean isBalanced = postingEngineService.validateJournalBalance(journalId);
        return ResponseEntity.ok(isBalanced);
    }

    @GetMapping("/pending-posting")
    @PreAuthorize("hasRole('FINANCE_MANAGER')")
    @Operation(summary = "Get journals pending posting", description = "Retrieves approved journals that are ready for posting")
    public ResponseEntity<List<JournalHeaderResponse>> getJournalsPendingPosting() {
        
        List<JournalHeader> journals = postingEngineService.getJournalsForPosting(
            com.financial.corefinance.domain.base.TenantContext.getCurrentTenant());
        List<JournalHeaderResponse> response = journals.stream()
            .map(journalMapper::toJournalHeaderResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}
