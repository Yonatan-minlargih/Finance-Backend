package com.financial.corefinance.mapper;

import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.domain.entity.JournalLine;
import com.financial.corefinance.dto.request.JournalHeaderRequest;
import com.financial.corefinance.dto.request.JournalLineRequest;
import com.financial.corefinance.dto.response.JournalHeaderResponse;
import com.financial.corefinance.dto.response.JournalLineResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JournalMapper {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "postedAt", ignore = true)
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "rejectedBy", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "isReversed", ignore = true)
    @Mapping(target = "reversedBy", ignore = true)
    @Mapping(target = "reversedAt", ignore = true)
    @Mapping(target = "reversalReason", ignore = true)
    @Mapping(target = "originalJournalId", ignore = true)
    @Mapping(target = "totalDebit", ignore = true)
    @Mapping(target = "totalCredit", ignore = true)
    @Mapping(target = "attachmentCount", ignore = true)
    @Mapping(target = "journalLines", source = "journalLines", qualifiedByName = "mapJournalLines")
    JournalHeader toJournalHeader(JournalHeaderRequest request);

    @Mapping(target = "journalHeaderId", ignore = true)
    @Mapping(target = "lineNumber", ignore = true)
    @Mapping(target = "reconciled", ignore = true)
    @Mapping(target = "reconciledAt", ignore = true)
    @Mapping(target = "reconciledBy", ignore = true)
    JournalLine toJournalLine(JournalLineRequest request);

    @Mapping(target = "accountingPeriodName", ignore = true)
    @Mapping(target = "journalLines", source = "journalLines", qualifiedByName = "mapJournalLineResponses")
    JournalHeaderResponse toJournalHeaderResponse(JournalHeader journalHeader);

    @Mapping(target = "accountCode", source = "account.accountCode")
    @Mapping(target = "accountName", source = "account.accountName")
    JournalLineResponse toJournalLineResponse(JournalLine journalLine);

    @Named("mapJournalLines")
    List<JournalLine> mapJournalLines(List<JournalLineRequest> journalLineRequests);

    @Named("mapJournalLineResponses")
    List<JournalLineResponse> mapJournalLineResponses(List<JournalLine> journalLines);

    void updateJournalHeaderFromRequest(JournalHeaderRequest request, @MappingTarget JournalHeader journalHeader);

    default JournalLine updateJournalLineFromRequest(JournalLineRequest request, JournalLine existingLine) {
        if (request.getDebitAmount() != null) {
            existingLine.setDebitAmount(request.getDebitAmount());
        }
        if (request.getCreditAmount() != null) {
            existingLine.setCreditAmount(request.getCreditAmount());
        }
        if (request.getDescription() != null) {
            existingLine.setDescription(request.getDescription());
        }
        if (request.getCostCenterId() != null) {
            existingLine.setCostCenterId(request.getCostCenterId());
        }
        if (request.getDepartmentId() != null) {
            existingLine.setDepartmentId(request.getDepartmentId());
        }
        if (request.getProjectId() != null) {
            existingLine.setProjectId(request.getProjectId());
        }
        if (request.getProductId() != null) {
            existingLine.setProductId(request.getProductId());
        }
        if (request.getLocationId() != null) {
            existingLine.setLocationId(request.getLocationId());
        }
        if (request.getAnalysisCode() != null) {
            existingLine.setAnalysisCode(request.getAnalysisCode());
        }
        if (request.getReferenceNumber() != null) {
            existingLine.setReferenceNumber(request.getReferenceNumber());
        }
        if (request.getReferenceType() != null) {
            existingLine.setReferenceType(request.getReferenceType());
        }
        if (request.getReferenceId() != null) {
            existingLine.setReferenceId(request.getReferenceId());
        }
        if (request.getTaxCode() != null) {
            existingLine.setTaxCode(request.getTaxCode());
        }
        if (request.getTaxRate() != null) {
            existingLine.setTaxRate(request.getTaxRate());
        }
        if (request.getTaxAmount() != null) {
            existingLine.setTaxAmount(request.getTaxAmount());
        }
        if (request.getCurrencyCode() != null) {
            existingLine.setCurrencyCode(request.getCurrencyCode());
        }
        if (request.getExchangeRate() != null) {
            existingLine.setExchangeRate(request.getExchangeRate());
        }
        if (request.getForeignDebitAmount() != null) {
            existingLine.setForeignDebitAmount(request.getForeignDebitAmount());
        }
        if (request.getForeignCreditAmount() != null) {
            existingLine.setForeignCreditAmount(request.getForeignCreditAmount());
        }
        return existingLine;
    }
}
