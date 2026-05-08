package com.financial.corefinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.corefinance.dto.request.JournalHeaderRequest;
import com.financial.corefinance.dto.response.JournalHeaderResponse;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.mapper.JournalMapper;
import com.financial.corefinance.service.PostingEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JournalController.class)
class JournalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostingEngineService postingEngineService;

    @MockBean
    private JournalMapper journalMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"ACCOUNTANT"})
    void createAndPostJournal_ValidRequest_ShouldReturnCreated() throws Exception {
        // Given
        JournalHeaderRequest request = JournalHeaderRequest.builder()
            .journalDate(LocalDate.of(2024, 1, 15))
            .accountingPeriodId(UUID.randomUUID())
            .journalType(JournalHeader.JournalType.MANUAL)
            .referenceNumber("REF-001")
            .description("Test Journal")
            .narration("Test journal entry")
            .build();

        JournalHeader postedJournal = JournalHeader.builder()
            .id(UUID.randomUUID())
            .journalNumber("JRN-001001")
            .status(JournalHeader.JournalStatus.POSTED)
            .description("Test Journal")
            .build();

        JournalHeaderResponse response = JournalHeaderResponse.builder()
            .journalId(postedJournal.getId())
            .journalNumber(postedJournal.getJournalNumber())
            .status(postedJournal.getStatus().toString())
            .description(postedJournal.getDescription())
            .build();

        when(journalMapper.toJournalHeader(any(JournalHeaderRequest.class))).thenReturn(new JournalHeader());
        when(postingEngineService.createAndPostJournal(any(JournalHeader.class))).thenReturn(postedJournal);
        when(journalMapper.toJournalHeaderResponse(any(JournalHeader.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/journals")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.journalId").value(postedJournal.getId().toString()))
                .andExpect(jsonPath("$.journalNumber").value("JRN-001001"))
                .andExpect(jsonPath("$.status").value("POSTED"))
                .andExpect(jsonPath("$.description").value("Test Journal"));
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTANT"})
    void saveDraftJournal_ValidRequest_ShouldReturnCreated() throws Exception {
        // Given
        JournalHeaderRequest request = JournalHeaderRequest.builder()
            .journalDate(LocalDate.of(2024, 1, 15))
            .accountingPeriodId(UUID.randomUUID())
            .journalType(JournalHeader.JournalType.MANUAL)
            .description("Draft Journal")
            .build();

        JournalHeader draftJournal = JournalHeader.builder()
            .id(UUID.randomUUID())
            .status(JournalHeader.JournalStatus.DRAFT)
            .description("Draft Journal")
            .build();

        JournalHeaderResponse response = JournalHeaderResponse.builder()
            .journalId(draftJournal.getId())
            .status("DRAFT")
            .description(draftJournal.getDescription())
            .build();

        when(journalMapper.toJournalHeader(any(JournalHeaderRequest.class))).thenReturn(new JournalHeader());
        when(postingEngineService.saveDraftJournal(any(JournalHeader.class))).thenReturn(draftJournal);
        when(journalMapper.toJournalHeaderResponse(any(JournalHeader.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/journals/draft")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.journalId").value(draftJournal.getId().toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @WithMockUser(roles = {"FINANCE_MANAGER"})
    void postJournal_ValidJournalId_ShouldReturnOk() throws Exception {
        // Given
        UUID journalId = UUID.randomUUID();
        JournalHeader postedJournal = JournalHeader.builder()
            .id(journalId)
            .journalNumber("JRN-001001")
            .status(JournalHeader.JournalStatus.POSTED)
            .build();

        JournalHeaderResponse response = JournalHeaderResponse.builder()
            .journalId(journalId)
            .journalNumber("JRN-001001")
            .status("POSTED")
            .build();

        when(postingEngineService.postJournal(journalId)).thenReturn(postedJournal);
        when(journalMapper.toJournalHeaderResponse(any(JournalHeader.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/journals/{journalId}/post", journalId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.journalId").value(journalId.toString()))
                .andExpect(jsonPath("$.status").value("POSTED"));
    }

    @Test
    @WithMockUser(roles = {"FINANCE_MANAGER"})
    void reverseJournal_ValidJournalId_ShouldReturnOk() throws Exception {
        // Given
        UUID journalId = UUID.randomUUID();
        String reversalReason = "Test reversal";
        JournalHeader reversalJournal = JournalHeader.builder()
            .id(UUID.randomUUID())
            .journalNumber("JRN-001002")
            .status(JournalHeader.JournalStatus.POSTED)
            .journalType(JournalHeader.JournalType.REVERSAL)
            .build();

        JournalHeaderResponse response = JournalHeaderResponse.builder()
            .journalId(reversalJournal.getId())
            .journalNumber("JRN-001002")
            .status("POSTED")
            .journalType("REVERSAL")
            .build();

        when(postingEngineService.reverseJournal(journalId, reversalReason)).thenReturn(reversalJournal);
        when(journalMapper.toJournalHeaderResponse(any(JournalHeader.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/journals/{journalId}/reverse", journalId)
                .with(csrf())
                .param("reversalReason", reversalReason))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.journalNumber").value("JRN-001002"))
                .andExpect(jsonPath("$.status").value("POSTED"))
                .andExpect(jsonPath("$.journalType").value("REVERSAL"));
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTANT"})
    void getJournal_ValidJournalId_ShouldReturnOk() throws Exception {
        // Given
        UUID journalId = UUID.randomUUID();
        JournalHeaderResponse response = JournalHeaderResponse.builder()
            .journalId(journalId)
            .journalNumber("JRN-001001")
            .status("POSTED")
            .build();

        // This would need to be implemented in a service
        // when(journalService.getJournalById(journalId)).thenReturn(Optional.of(journal));
        // when(journalMapper.toJournalHeaderResponse(any(JournalHeader.class))).thenReturn(response);

        // When & Then - Placeholder test
        mockMvc.perform(get("/api/v1/journals/{journalId}", journalId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTANT"})
    void getAllJournals_ShouldReturnPage() throws Exception {
        // Given - This would need to be implemented in a service
        // Page<JournalHeader> journals = new PageImpl<>(Arrays.asList(journal1, journal2));
        // when(journalService.getAllJournals(any(Pageable.class))).thenReturn(journals);
        // when(journalMapper.toJournalHeaderResponse(any(JournalHeader.class))).thenReturn(response);

        // When & Then - Placeholder test
        mockMvc.perform(get("/api/v1/journals")
                .param("page", "0")
                .param("size", "20")
                .param("sort", "createdAt")
                .param("direction", "desc"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTANT"})
    void searchJournals_ValidSearchTerm_ShouldReturnResults() throws Exception {
        // Given
        String searchTerm = "cash";

        // This would need to be implemented in a service
        // Page<JournalHeader> journals = new PageImpl<>(Arrays.asList(journal1));
        // when(journalService.searchJournals(eq(searchTerm), any(Pageable.class))).thenReturn(journals);
        // when(journalMapper.toJournalHeaderResponse(any(JournalHeader.class))).thenReturn(response);

        // When & Then - Placeholder test
        mockMvc.perform(get("/api/v1/journals/search")
                .param("search", searchTerm)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTANT"})
    void getJournalsByStatus_ValidStatus_ShouldReturnResults() throws Exception {
        // Given
        JournalHeader.JournalStatus status = JournalHeader.JournalStatus.POSTED;
        List<JournalHeaderResponse> responses = Arrays.asList(
            JournalHeaderResponse.builder()
                .journalId(UUID.randomUUID())
                .status("POSTED")
                .build()
        );

        // This would need to be implemented in a service
        // List<JournalHeader> journals = Arrays.asList(journal1);
        // when(journalService.getJournalsByStatus(status)).thenReturn(journals);
        // when(journalMapper.toJournalHeaderResponse(any(JournalHeader.class))).thenReturn(response);

        // When & Then - Placeholder test
        mockMvc.perform(get("/api/v1/journals/status/{status}", status))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTANT"})
    void validateJournalBalance_ValidJournalId_ShouldReturnTrue() throws Exception {
        // Given
        UUID journalId = UUID.randomUUID();
        when(postingEngineService.validateJournalBalance(journalId)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/journals/{journalId}/validate", journalId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = {"FINANCE_MANAGER"})
    void getJournalsPendingPosting_ShouldReturnResults() throws Exception {
        // Given
        List<JournalHeader> pendingJournals = Arrays.asList(
            JournalHeader.builder()
                .id(UUID.randomUUID())
                .status(JournalHeader.JournalStatus.APPROVED)
                .build()
        );

        List<JournalHeaderResponse> responses = Arrays.asList(
            JournalHeaderResponse.builder()
                .journalId(pendingJournals.get(0).getId())
                .status("APPROVED")
                .build()
        );

        when(postingEngineService.getJournalsForPosting(any())).thenReturn(pendingJournals);
        when(journalMapper.toJournalHeaderResponse(any(JournalHeader.class))).thenReturn(responses.get(0));

        // When & Then
        mockMvc.perform(get("/api/v1/journals/pending-posting")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = {"USER"}) // User without required role
    void createJournal_InsufficientRole_ShouldReturnForbidden() throws Exception {
        // Given
        JournalHeaderRequest request = JournalHeaderRequest.builder()
            .journalDate(LocalDate.of(2024, 1, 15))
            .accountingPeriodId(UUID.randomUUID())
            .description("Test Journal")
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/journals")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createJournal_NoAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Given
        JournalHeaderRequest request = JournalHeaderRequest.builder()
            .journalDate(LocalDate.of(2024, 1, 15))
            .accountingPeriodId(UUID.randomUUID())
            .description("Test Journal")
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/journals")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ACCOUNTANT"})
    void createJournal_InvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        JournalHeaderRequest invalidRequest = JournalHeaderRequest.builder()
            .journalDate(null) // Missing required field
            .description("Test Journal")
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/journals")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
