package uk.gov.justice.laa.crime.assessmentservice.audit.internal.service;

import lombok.RequiredArgsConstructor;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventRequest;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditIdentifier;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditIdentifierType;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.entity.IojAuditEventEntity;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.repository.IojAuditEventRepository;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class IojAuditEventPersister implements AuditEventPersister {

    private final ObjectMapper objectMapper;
    private final IojAuditEventRepository repository;

    @Override
    public void persist(AuditEventRequest request) {

        UUID appealUuid = request.getIdentifierByName(AuditIdentifierType.APPEAL_ID)
                .map(AuditIdentifier::value)
                .map(UUID::fromString)
                .orElse(null);

        Long legacyId = request.getIdentifierByName(AuditIdentifierType.LEGACY_APPEAL_ID)
                .map(AuditIdentifier::value)
                .map(Long::parseLong)
                .orElse(null);

        IojAuditEventEntity entity = IojAuditEventEntity.builder()
                .appealId(appealUuid)
                .legacyAppealId(legacyId)
                .eventType(request.eventType())
                .auditPayload(toJsonOrNull(request.auditPayload()))
                .triggeredBy(request.triggeredBy())
                .traceId(request.traceId())
                .build();

        repository.save(entity);
    }

    private JsonNode toJsonOrNull(Object payload) {
        if (payload == null) {
            return null;
        }
        return objectMapper.valueToTree(payload);
    }
}
