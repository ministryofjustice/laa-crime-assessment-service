package uk.gov.justice.laa.crime.assessmentservice.audit.api;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public final class AuditRequests {

    public static AuditEventRequest findIojByAppealId(
            UUID appealId, String triggeredBy, String traceId, Map<String, Object> payload) {

        var requestBuilder = base(AuditEventType.FIND, triggeredBy, traceId, payload);

        if (appealId != null) {
            requestBuilder.identifiers(
                    List.of(new AuditIdentifier(AuditIdentifierType.APPEAL_ID, appealId.toString())));
        }

        return requestBuilder.build();
    }

    public static AuditEventRequest findIojByLegacyId(
            int legacyAppealId, String triggeredBy, String traceId, Map<String, Object> payload) {

        return base(AuditEventType.FIND, triggeredBy, traceId, payload)
                .identifiers(List.of(
                        new AuditIdentifier(AuditIdentifierType.LEGACY_APPEAL_ID, String.valueOf(legacyAppealId))))
                .build();
    }

    public static AuditEventRequest createIoj(
            UUID appealId, int legacyAppealId, String triggeredBy, String traceId, Map<String, Object> payload) {

        return base(AuditEventType.CREATE, triggeredBy, traceId, payload)
                .identifiers(List.of(
                        new AuditIdentifier(AuditIdentifierType.APPEAL_ID, appealId.toString()),
                        new AuditIdentifier(AuditIdentifierType.LEGACY_APPEAL_ID, String.valueOf(legacyAppealId))))
                .build();
    }

    private static AuditEventRequest.AuditEventRequestBuilder base(
            AuditEventType eventType, String triggeredBy, String traceId, Map<String, Object> payload) {

        return AuditEventRequest.builder()
                .domain(AuditDomain.IOJ_APPEAL)
                .eventType(eventType)
                .triggeredBy(triggeredBy)
                .traceId(traceId)
                .auditPayload(payload);
    }
}
