package com.financial.corefinance.domain.entity;

import com.financial.corefinance.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "integration_ingress_logs",
        indexes = {
                @Index(name = "idx_ingress_tenant_source", columnList = "tenant_id,source_system"),
                @Index(name = "idx_ingress_event", columnList = "event_id"),
                @Index(name = "idx_ingress_idempotency", columnList = "idempotency_key")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_ingress_tenant_idempotency", columnNames = {"tenant_id", "idempotency_key"}),
                @UniqueConstraint(name = "uq_ingress_tenant_event", columnNames = {"tenant_id", "event_id"})
        })
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IntegrationIngressLog extends BaseEntity {

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Column(name = "contract_version", nullable = false, length = 20)
    private String contractVersion;

    @Column(name = "source_system", nullable = false, length = 50)
    private String sourceSystem;

    @Column(name = "correlation_id", length = 120)
    private String correlationId;

    @Column(name = "journal_id")
    private UUID journalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IngressStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public enum IngressStatus {
        ACCEPTED,
        PROCESSED,
        FAILED
    }
}