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
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_tenant", columnList = "tenant_id"),
    @Index(name = "idx_audit_logs_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_audit_logs_user", columnList = "user_id"),
    @Index(name = "idx_audit_logs_timestamp", columnList = "created_at")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseEntity {

    @NotNull(message = "Entity type is required")
    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    @NotNull(message = "Entity ID is required")
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @NotNull(message = "Action is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private AuditAction action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "user_email", length = 200)
    private String userEmail;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "operation_result", length = 20)
    @Builder.Default
    private String operationResult = "SUCCESS"; // SUCCESS, FAILURE, PARTIAL

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @Column(name = "module_name", length = 50)
    private String moduleName;

    @Column(name = "function_name", length = 100)
    private String functionName;

    @Column(name = "business_transaction_id", length = 100)
    private String businessTransactionId;

    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE,
        VIEW,
        LOGIN,
        LOGOUT,
        POST,
        REVERSE,
        APPROVE,
        REJECT,
        EXPORT,
        PRINT,
        EMAIL_SEND
    }
}
