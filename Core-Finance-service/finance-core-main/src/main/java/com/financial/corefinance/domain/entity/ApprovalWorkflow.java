package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "approval_workflows", indexes = {
    @Index(name = "idx_approval_workflows_tenant", columnList = "tenant_id"),
    @Index(name = "idx_approval_workflows_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_approval_workflows_status", columnList = "status")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApprovalWorkflow extends BaseEntity {

    @NotNull(message = "Entity type is required")
    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    @NotNull(message = "Entity ID is required")
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @NotNull(message = "Workflow type is required")
    @Column(name = "workflow_type", length = 50, nullable = false)
    private String workflowType;

    @Column(name = "workflow_name", length = 200)
    private String workflowName;

    @NotNull(message = "Initiated by is required")
    @Column(name = "initiated_by", length = 100, nullable = false)
    private String initiatedBy;

    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WorkflowStatus status = WorkflowStatus.PENDING;

    @Column(name = "current_step")
    @Builder.Default
    private Integer currentStep = 1;

    @Column(name = "total_steps")
    private Integer totalSteps;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completed_by", length = 100)
    private String completedBy;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "comments", length = 2000)
    private String comments;

    @Builder.Default
    @OneToMany(mappedBy = "approvalWorkflow", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApprovalStep> approvalSteps = new ArrayList<>();

    public enum WorkflowStatus {
        PENDING,
        IN_PROGRESS,
        APPROVED,
        REJECTED,
        CANCELLED,
        COMPLETED,
        EXPIRED
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
