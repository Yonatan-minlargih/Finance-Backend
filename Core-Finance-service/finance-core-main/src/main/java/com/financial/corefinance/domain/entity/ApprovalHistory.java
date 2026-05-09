package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "approval_history", indexes = {
    @Index(name = "idx_approval_history_tenant", columnList = "tenant_id"),
    @Index(name = "idx_approval_history_step", columnList = "approval_step_id"),
    @Index(name = "idx_approval_history_timestamp", columnList = "created_at")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApprovalHistory extends BaseEntity {

    @NotNull(message = "Approval step is required")
    @Column(name = "approval_step_id", nullable = false)
    private UUID approvalStepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_step_id", insertable = false, updatable = false)
    private ApprovalStep approvalStep;

    @NotNull(message = "Action is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private ApprovalStep.Action action;

    @Column(name = "actioned_by", length = 100)
    private String actionedBy;

    @Column(name = "actioned_at")
    private LocalDateTime actionedAt;

    @Column(name = "comments", length = 2000)
    private String comments;

    @Column(name = "old_status", length = 20)
    private String oldStatus;

    @Column(name = "new_status", length = 20)
    private String newStatus;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;
}
