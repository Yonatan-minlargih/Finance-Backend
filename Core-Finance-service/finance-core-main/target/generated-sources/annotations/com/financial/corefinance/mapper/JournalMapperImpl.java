package com.financial.corefinance.mapper;

import com.financial.corefinance.domain.entity.Account;
import com.financial.corefinance.domain.entity.JournalHeader;
import com.financial.corefinance.domain.entity.JournalLine;
import com.financial.corefinance.dto.request.JournalHeaderRequest;
import com.financial.corefinance.dto.request.JournalLineRequest;
import com.financial.corefinance.dto.response.JournalHeaderResponse;
import com.financial.corefinance.dto.response.JournalLineResponse;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-17T04:24:35+0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class JournalMapperImpl implements JournalMapper {

    @Override
    public JournalHeader toJournalHeader(JournalHeaderRequest request) {
        if ( request == null ) {
            return null;
        }

        JournalHeader.JournalHeaderBuilder<?, ?> journalHeader = JournalHeader.builder();

        journalHeader.journalLines( mapJournalLines( request.getJournalLines() ) );
        journalHeader.accountingPeriodId( request.getAccountingPeriodId() );
        journalHeader.autoReverse( request.getAutoReverse() );
        journalHeader.autoReverseDate( request.getAutoReverseDate() );
        journalHeader.batchNumber( request.getBatchNumber() );
        journalHeader.description( request.getDescription() );
        journalHeader.journalDate( request.getJournalDate() );
        journalHeader.journalType( request.getJournalType() );
        journalHeader.narration( request.getNarration() );
        journalHeader.referenceId( request.getReferenceId() );
        journalHeader.referenceNumber( request.getReferenceNumber() );
        journalHeader.referenceType( request.getReferenceType() );
        journalHeader.sourceSystem( request.getSourceSystem() );

        return journalHeader.build();
    }

    @Override
    public JournalLine toJournalLine(JournalLineRequest request) {
        if ( request == null ) {
            return null;
        }

        JournalLine.JournalLineBuilder<?, ?> journalLine = JournalLine.builder();

        journalLine.accountId( request.getAccountId() );
        journalLine.analysisCode( request.getAnalysisCode() );
        journalLine.costCenterId( request.getCostCenterId() );
        journalLine.creditAmount( request.getCreditAmount() );
        journalLine.currencyCode( request.getCurrencyCode() );
        journalLine.debitAmount( request.getDebitAmount() );
        journalLine.departmentId( request.getDepartmentId() );
        journalLine.description( request.getDescription() );
        journalLine.exchangeRate( request.getExchangeRate() );
        journalLine.foreignCreditAmount( request.getForeignCreditAmount() );
        journalLine.foreignDebitAmount( request.getForeignDebitAmount() );
        journalLine.locationId( request.getLocationId() );
        journalLine.productId( request.getProductId() );
        journalLine.projectId( request.getProjectId() );
        journalLine.referenceId( request.getReferenceId() );
        journalLine.referenceNumber( request.getReferenceNumber() );
        journalLine.referenceType( request.getReferenceType() );
        journalLine.taxAmount( request.getTaxAmount() );
        journalLine.taxCode( request.getTaxCode() );
        journalLine.taxRate( request.getTaxRate() );

        return journalLine.build();
    }

    @Override
    public JournalHeaderResponse toJournalHeaderResponse(JournalHeader journalHeader) {
        if ( journalHeader == null ) {
            return null;
        }

        JournalHeaderResponse.JournalHeaderResponseBuilder journalHeaderResponse = JournalHeaderResponse.builder();

        journalHeaderResponse.journalId( journalHeader.getId() );
        journalHeaderResponse.journalLines( mapJournalLineResponses( journalHeader.getJournalLines() ) );
        journalHeaderResponse.accountingPeriodId( journalHeader.getAccountingPeriodId() );
        journalHeaderResponse.approvedAt( journalHeader.getApprovedAt() );
        journalHeaderResponse.approvedBy( journalHeader.getApprovedBy() );
        journalHeaderResponse.attachmentCount( journalHeader.getAttachmentCount() );
        journalHeaderResponse.autoReverse( journalHeader.getAutoReverse() );
        journalHeaderResponse.autoReverseDate( journalHeader.getAutoReverseDate() );
        journalHeaderResponse.batchNumber( journalHeader.getBatchNumber() );
        journalHeaderResponse.createdAt( journalHeader.getCreatedAt() );
        journalHeaderResponse.createdBy( journalHeader.getCreatedBy() );
        journalHeaderResponse.description( journalHeader.getDescription() );
        journalHeaderResponse.isReversed( journalHeader.getIsReversed() );
        journalHeaderResponse.journalDate( journalHeader.getJournalDate() );
        journalHeaderResponse.journalNumber( journalHeader.getJournalNumber() );
        journalHeaderResponse.journalType( journalHeader.getJournalType() );
        journalHeaderResponse.narration( journalHeader.getNarration() );
        journalHeaderResponse.originalJournalId( journalHeader.getOriginalJournalId() );
        journalHeaderResponse.postedAt( journalHeader.getPostedAt() );
        journalHeaderResponse.postedBy( journalHeader.getPostedBy() );
        journalHeaderResponse.referenceId( journalHeader.getReferenceId() );
        journalHeaderResponse.referenceNumber( journalHeader.getReferenceNumber() );
        journalHeaderResponse.referenceType( journalHeader.getReferenceType() );
        journalHeaderResponse.rejectedAt( journalHeader.getRejectedAt() );
        journalHeaderResponse.rejectedBy( journalHeader.getRejectedBy() );
        journalHeaderResponse.rejectionReason( journalHeader.getRejectionReason() );
        journalHeaderResponse.reversalReason( journalHeader.getReversalReason() );
        journalHeaderResponse.reversedAt( journalHeader.getReversedAt() );
        journalHeaderResponse.reversedBy( journalHeader.getReversedBy() );
        journalHeaderResponse.sourceSystem( journalHeader.getSourceSystem() );
        journalHeaderResponse.status( journalHeader.getStatus() );
        journalHeaderResponse.tenantId( journalHeader.getTenantId() );
        journalHeaderResponse.totalCredit( journalHeader.getTotalCredit() );
        journalHeaderResponse.totalDebit( journalHeader.getTotalDebit() );
        journalHeaderResponse.updatedAt( journalHeader.getUpdatedAt() );
        journalHeaderResponse.updatedBy( journalHeader.getUpdatedBy() );
        journalHeaderResponse.version( journalHeader.getVersion() );

        return journalHeaderResponse.build();
    }

    @Override
    public JournalLineResponse toJournalLineResponse(JournalLine journalLine) {
        if ( journalLine == null ) {
            return null;
        }

        JournalLineResponse journalLineResponse = new JournalLineResponse();

        journalLineResponse.setAccountCode( journalLineAccountAccountCode( journalLine ) );
        journalLineResponse.setAccountName( journalLineAccountAccountName( journalLine ) );
        journalLineResponse.setAccountId( journalLine.getAccountId() );
        journalLineResponse.setAnalysisCode( journalLine.getAnalysisCode() );
        journalLineResponse.setCostCenterId( journalLine.getCostCenterId() );
        journalLineResponse.setCreatedAt( journalLine.getCreatedAt() );
        journalLineResponse.setCreatedBy( journalLine.getCreatedBy() );
        journalLineResponse.setCreditAmount( journalLine.getCreditAmount() );
        journalLineResponse.setCurrencyCode( journalLine.getCurrencyCode() );
        journalLineResponse.setDebitAmount( journalLine.getDebitAmount() );
        journalLineResponse.setDepartmentId( journalLine.getDepartmentId() );
        journalLineResponse.setDescription( journalLine.getDescription() );
        journalLineResponse.setExchangeRate( journalLine.getExchangeRate() );
        journalLineResponse.setForeignCreditAmount( journalLine.getForeignCreditAmount() );
        journalLineResponse.setForeignDebitAmount( journalLine.getForeignDebitAmount() );
        journalLineResponse.setId( journalLine.getId() );
        journalLineResponse.setJournalHeaderId( journalLine.getJournalHeaderId() );
        journalLineResponse.setLineNumber( journalLine.getLineNumber() );
        journalLineResponse.setLocationId( journalLine.getLocationId() );
        journalLineResponse.setProductId( journalLine.getProductId() );
        journalLineResponse.setProjectId( journalLine.getProjectId() );
        journalLineResponse.setReconciled( journalLine.getReconciled() );
        journalLineResponse.setReconciledAt( journalLine.getReconciledAt() );
        journalLineResponse.setReconciledBy( journalLine.getReconciledBy() );
        journalLineResponse.setReferenceId( journalLine.getReferenceId() );
        journalLineResponse.setReferenceNumber( journalLine.getReferenceNumber() );
        journalLineResponse.setReferenceType( journalLine.getReferenceType() );
        journalLineResponse.setTaxAmount( journalLine.getTaxAmount() );
        journalLineResponse.setTaxCode( journalLine.getTaxCode() );
        journalLineResponse.setTaxRate( journalLine.getTaxRate() );
        journalLineResponse.setTenantId( journalLine.getTenantId() );
        journalLineResponse.setUpdatedAt( journalLine.getUpdatedAt() );
        journalLineResponse.setUpdatedBy( journalLine.getUpdatedBy() );
        journalLineResponse.setVersion( journalLine.getVersion() );

        return journalLineResponse;
    }

    @Override
    public List<JournalLine> mapJournalLines(List<JournalLineRequest> journalLineRequests) {
        if ( journalLineRequests == null ) {
            return null;
        }

        List<JournalLine> list = new ArrayList<JournalLine>( journalLineRequests.size() );
        for ( JournalLineRequest journalLineRequest : journalLineRequests ) {
            list.add( toJournalLine( journalLineRequest ) );
        }

        return list;
    }

    @Override
    public List<JournalLineResponse> mapJournalLineResponses(List<JournalLine> journalLines) {
        if ( journalLines == null ) {
            return null;
        }

        List<JournalLineResponse> list = new ArrayList<JournalLineResponse>( journalLines.size() );
        for ( JournalLine journalLine : journalLines ) {
            list.add( toJournalLineResponse( journalLine ) );
        }

        return list;
    }

    @Override
    public void updateJournalHeaderFromRequest(JournalHeaderRequest request, JournalHeader journalHeader) {
        if ( request == null ) {
            return;
        }

        journalHeader.setAccountingPeriodId( request.getAccountingPeriodId() );
        journalHeader.setAutoReverse( request.getAutoReverse() );
        journalHeader.setAutoReverseDate( request.getAutoReverseDate() );
        journalHeader.setBatchNumber( request.getBatchNumber() );
        journalHeader.setDescription( request.getDescription() );
        journalHeader.setJournalDate( request.getJournalDate() );
        if ( journalHeader.getJournalLines() != null ) {
            List<JournalLine> list = journalLineRequestListToJournalLineList( request.getJournalLines() );
            if ( list != null ) {
                journalHeader.getJournalLines().clear();
                journalHeader.getJournalLines().addAll( list );
            }
            else {
                journalHeader.setJournalLines( null );
            }
        }
        else {
            List<JournalLine> list = journalLineRequestListToJournalLineList( request.getJournalLines() );
            if ( list != null ) {
                journalHeader.setJournalLines( list );
            }
        }
        journalHeader.setJournalType( request.getJournalType() );
        journalHeader.setNarration( request.getNarration() );
        journalHeader.setReferenceId( request.getReferenceId() );
        journalHeader.setReferenceNumber( request.getReferenceNumber() );
        journalHeader.setReferenceType( request.getReferenceType() );
        journalHeader.setSourceSystem( request.getSourceSystem() );
    }

    private String journalLineAccountAccountCode(JournalLine journalLine) {
        if ( journalLine == null ) {
            return null;
        }
        Account account = journalLine.getAccount();
        if ( account == null ) {
            return null;
        }
        String accountCode = account.getAccountCode();
        if ( accountCode == null ) {
            return null;
        }
        return accountCode;
    }

    private String journalLineAccountAccountName(JournalLine journalLine) {
        if ( journalLine == null ) {
            return null;
        }
        Account account = journalLine.getAccount();
        if ( account == null ) {
            return null;
        }
        String accountName = account.getAccountName();
        if ( accountName == null ) {
            return null;
        }
        return accountName;
    }

    protected List<JournalLine> journalLineRequestListToJournalLineList(List<JournalLineRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<JournalLine> list1 = new ArrayList<JournalLine>( list.size() );
        for ( JournalLineRequest journalLineRequest : list ) {
            list1.add( toJournalLine( journalLineRequest ) );
        }

        return list1;
    }
}
