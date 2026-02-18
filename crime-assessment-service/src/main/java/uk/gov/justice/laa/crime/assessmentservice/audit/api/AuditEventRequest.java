package uk.gov.justice.laa.crime.assessmentservice.audit.api;

import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
public record AuditEventRequest(
        AuditDomain domain,
        List<AuditIdentifier> identifiers,
        AuditEventType eventType,
        Object auditPayload, // nullable (e.g. FIND)
        String triggeredBy,
        String traceId // nullable if not always available
        ) {

    public Optional<AuditIdentifier> getIdentifierByName(AuditIdentifierType type) {
        if (identifiers == null) return Optional.empty();
        return identifiers.stream().filter(id -> id.type() == type).findFirst();
    }
}
