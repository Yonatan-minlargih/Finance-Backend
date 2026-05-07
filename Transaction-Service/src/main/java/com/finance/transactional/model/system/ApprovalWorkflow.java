package com.finance.transactional.model.system;

import com.finance.transactional.model.BaseTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approval_workflows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalWorkflow extends BaseTenantEntity {

    @Column(name = "document_type", length = 50, nullable = false)
    private String documentType; // INVOICE, PAYMENT, PO

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "approver_role", length = 100, nullable = false)
    private String approverRole;

    @Column(name = "approve_condition", columnDefinition = "JSON")
    private String condition; // e.g. amount thresholds
}
