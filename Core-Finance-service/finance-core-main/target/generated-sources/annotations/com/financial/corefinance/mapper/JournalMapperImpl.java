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
    date = "2026-05-09T14:38:28+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
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
        journalHeader.journalDate( request.getJournalDate() );
        journalHeader.accountingPeriodId( request.getAccountingPeriodId() );
        journalHeader.journalType( request.getJournalType() );
        journalHeader.referenceNumber( request.getReferenceNumber() );
        journalHeader.referenceType( request.getReferenceType() );
        journalHeader.referenceId( request.getReferenceId() );
        journalHeader.description( request.getDescription() );
        journalHeader.narration( request.getNarration() );
        journalHeader.autoReverse( request.getAutoReverse() );
        journalHeader.autoReverseDate( request.getAutoReverseDate() );
        journalHeader.batchNumber( request.getBatchNumber() );
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
        journalLine.debitAmount( request.getDebitAmount() );
        journalLine.creditAmount( request.getCreditAmount() );
        journalLine.description( request.getDescription() );
        journalLine.costCenterId( request.getCostCenterId() );
        journalLine.departmentId( request.getDepartmentId() );
        journalLine.projectId( request.getProjectId() );
        journalLine.productId( request.getProductId() );
        journalLine.locationId( request.getLocationId() );
        journalLine.analysisCode( request.getAnalysisCode() );
        journalLine.referenceNumber( request.getReferenceNumber() );
        journalLine.referenceType( request.getReferenceType() );
        journalLine.referenceId( request.getReferenceId() );
        journalLine.taxCode( request.getTaxCode() );
        journalLine.taxRate( request.getTaxRate() );
        journalLine.taxAmount( request.getTaxAmount() );
        journalLine.currencyCode( request.getCurrencyCode() );
        journalLine.exchangeRate( request.getExchangeRate() );
        journalLine.foreignDebitAmount( request.getForeignDebitAmount() );
        journalLine.foreignCreditAmount( request.getForeignCreditAmount() );

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
        journalHeaderResponse.tenantId( journalHeader.getTenantId() );
        journalHeaderResponse.journalNumber( journalHeader.getJournalNumber() );
        journalHeaderResponse.createdAt( journalHeader.getCreatedAt() );
        journalHeaderResponse.updatedAt( journalHeader.getUpdatedAt() );
        journalHeaderResponse.createdBy( journalHeader.getCreatedBy() );
        journalHeaderResponse.updatedBy( journalHeader.getUpdatedBy() );
        journalHeaderResponse.version( journalHeader.getVersion() );
        journalHeaderResponse.journalDate( journalHeader.getJournalDate() );
        journalHeaderResponse.accountingPeriodId( journalHeader.getAccountingPeriodId() );
        journalHeaderResponse.journalType( journalHeader.getJournalType() );
        journalHeaderResponse.referenceNumber( journalHeader.getReferenceNumber() );
        journalHeaderResponse.referenceType( journalHeader.getReferenceType() );
        journalHeaderResponse.referenceId( journalHeader.getReferenceId() );
        journalHeaderResponse.description( journalHeader.getDescription() );
        journalHeaderResponse.narration( journalHeader.getNarration() );
        journalHeaderResponse.totalDebit( journalHeader.getTotalDebit() );
        journalHeaderResponse.totalCredit( journalHeader.getTotalCredit() );
        journalHeaderResponse.status( journalHeader.getStatus() );
        journalHeaderResponse.postedAt( journalHeader.getPostedAt() );
        journalHeaderResponse.postedBy( journalHeader.getPostedBy() );
        journalHeaderResponse.approvedAt( journalHeader.getApprovedAt() );
        journalHeaderResponse.approvedBy( journalHeader.getApprovedBy() );
        journalHeaderResponse.rejectedAt( journalHeader.getRejectedAt() );
        journalHeaderResponse.rejectedBy( journalHeader.getRejectedBy() );
        journalHeaderResponse.rejectionReason( journalHeader.getRejectionReason() );
        journalHeaderResponse.isReversed( journalHeader.getIsReversed() );
        journalHeaderResponse.reversedBy( journalHeader.getReversedBy() );
        journalHeaderResponse.reversedAt( journalHeader.getReversedAt() );
        journalHeaderResponse.reversalReason( journalHeader.getReversalReason() );
        journalHeaderResponse.originalJournalId( journalHeader.getOriginalJournalId() );
        journalHeaderResponse.autoReverse( journalHeader.getAutoReverse() );
        journalHeaderResponse.autoReverseDate( journalHeader.getAutoReverseDate() );
        journalHeaderResponse.batchNumber( journalHeader.getBatchNumber() );
        journalHeaderResponse.sourceSystem( journalHeader.getSourceSystem() );
        journalHeaderResponse.attachmentCount( journalHeader.getAttachmentCount() );

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
        journalLineResponse.setId( journalLine.getId() );
        journalLineResponse.setTenantId( journalLine.getTenantId() );
        journalLineResponse.setCreatedAt( journalLine.getCreatedAt() );
        journalLineResponse.setUpdatedAt( journalLine.getUpdatedAt() );
        journalLineResponse.setCreatedBy( journalLine.getCreatedBy() );
        journalLineResponse.setUpdatedBy( journalLine.getUpdatedBy() );
        journalLineResponse.setVersion( journalLine.getVersion() );
        journalLineResponse.setJournalHeaderId( journalLine.getJournalHeaderId() );
        journalLineResponse.setLineNumber( journalLine.getLineNumber() );
        journalLineResponse.setAccountId( journalLine.getAccountId() );
        journalLineResponse.setDebitAmount( journalLine.getDebitAmount() );
        journalLineResponse.setCreditAmount( journalLine.getCreditAmount() );
        journalLineResponse.setDescription( journalLine.getDescription() );
        journalLineResponse.setCostCenterId( journalLine.getCostCenterId() );
        journalLineResponse.setDepartmentId( journalLine.getDepartmentId() );
        journalLineResponse.setProjectId( journalLine.getProjectId() );
        journalLineResponse.setProductId( journalLine.getProductId() );
        journalLineResponse.setLocationId( journalLine.getLocationId() );
        journalLineResponse.setAnalysisCode( journalLine.getAnalysisCode() );
        journalLineResponse.setReferenceNumber( journalLine.getReferenceNumber() );
        journalLineResponse.setReferenceType( journalLine.getReferenceType() );
        journalLineResponse.setReferenceId( journalLine.getReferenceId() );
        journalLineResponse.setTaxCode( journalLine.getTaxCode() );
        journalLineResponse.setTaxRate( journalLine.getTaxRate() );
        journalLineResponse.setTaxAmount( journalLine.getTaxAmount() );
        journalLineResponse.setCurrencyCode( journalLine.getCurrencyCode() );
        journalLineResponse.setExchangeRate( journalLine.getExchangeRate() );
        journalLineResponse.setForeignDebitAmount( journalLine.getForeignDebitAmount() );
        journalLineResponse.setForeignCreditAmount( journalLine.getForeignCreditAmount() );
        journalLineResponse.setReconciled( journalLine.getReconciled() );
        journalLineResponse.setReconciledAt( journalLine.getReconciledAt() );
        journalLineResponse.setReconciledBy( journalLine.getReconciledBy() );

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

        journalHeader.setJournalDate( request.getJournalDate() );
        journalHeader.setAccountingPeriodId( request.getAccountingPeriodId() );
        journalHeader.setJournalType( request.getJournalType() );
        journalHeader.setReferenceNumber( request.getReferenceNumber() );
        journalHeader.setReferenceType( request.getReferenceType() );
        journalHeader.setReferenceId( request.getReferenceId() );
        journalHeader.setDescription( request.getDescription() );
        journalHeader.setNarration( request.getNarration() );
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
        journalHeader.setAutoReverse( request.getAutoReverse() );
        journalHeader.setAutoReverseDate( request.getAutoReverseDate() );
        journalHeader.setBatchNumber( request.getBatchNumber() );
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
