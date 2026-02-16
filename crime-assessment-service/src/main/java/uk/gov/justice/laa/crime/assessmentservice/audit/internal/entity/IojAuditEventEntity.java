package uk.gov.justice.laa.crime.assessmentservice.audit.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventType;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.databind.JsonNode;

@Getter
@Setter
@Builder
@Entity
@Immutable
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ioj_appeal_event", schema = "ioj_appeal")
public class IojAuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "appeal_id")
    private UUID appealId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AuditEventType eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "audit_payload", columnDefinition = "jsonb")
    private JsonNode auditPayload;

    @Column(name = "triggered_by", nullable = false)
    private String triggeredBy;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "legacy_appeal_id")
    private Long legacyAppealId;

    @CreatedDate
    @Column(name = "triggered_at", nullable = false, updatable = false)
    private Instant triggeredAt;
}
