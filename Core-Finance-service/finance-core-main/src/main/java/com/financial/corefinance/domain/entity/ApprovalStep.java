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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "approval_steps", indexes = {
    @Index(name = "idx_approval_steps_tenant", columnList = "tenant_id"),
    @Index(name = "idx_approval_steps_workflow", columnList = "approval_workflow_id"),
    @Index(name = "idx_approval_steps_assignee", columnList = "assignee_id"),
    @Index(name = "idx_approval_steps_status", columnList = "status")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApprovalStep extends BaseEntity {

    @NotNull(message = "Approval workflow is required")
    @Column(name = "approval_workflow_id", nullable = false)
    private UUID approvalWorkflowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_workflow_id", insertable = false, updatable = false)
    private ApprovalWorkflow approvalWorkflow;

    @NotNull(message = "Step number is required")
    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(name = "step_name", length = 200)
    private String stepName;

    @NotNull(message = "Assignee ID is required")
    @Column(name = "assignee_id", length = 100, nullable = false)
    private String assigneeId;

    @Column(name = "assignee_name", length = 100)
    private String assigneeName;

    @Column(name = "assignee_email", length = 200)
    private String assigneeEmail;

    @Column(name = "role_name", length = 100)
    private String roleName;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StepStatus status = StepStatus.PENDING;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "actioned_at")
    private LocalDateTime actionedAt;

    @Column(name = "actioned_by", length = 100)
    private String actionedBy;

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private Action action;

    @Column(name = "comments", length = 2000)
    private String comments;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "reminder_sent")
    @Builder.Default
    private Boolean reminderSent = false;

    @Column(name = "reminder_count")
    @Builder.Default
    private Integer reminderCount = 0;

    @Column(name = "escalated")
    @Builder.Default
    private Boolean escalated = false;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalated_to", length = 100)
    private String escalatedTo;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = true;

    @Column(name = "approval_type", length = 20)
    @Builder.Default
    private String approvalType = "ANYONE"; // ANYONE, ALL, MAJORITY

    @Builder.Default
    @OneToMany(mappedBy = "approvalStep", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApprovalHistory> approvalHistory = new ArrayList<>();

    public enum StepStatus {
        PENDING,
        ASSIGNED,
        IN_PROGRESS,
        APPROVED,
        REJECTED,
        SKIPPED,
        CANCELLED
    }

    public enum Action {
        APPROVE,
        REJECT,
        SKIP,
        ESCALATE,
        REQUEST_INFO
    }
}
