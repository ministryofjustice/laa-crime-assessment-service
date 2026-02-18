package uk.gov.justice.laa.crime.assessmentservice.audit.internal.mapper;

import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditOutcome;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditPath;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AuditPayloads {

    private AuditPayloads() {}

    public static Map<String, Object> findPayload(AuditOutcome outcome, AuditPath path) {
        return envelope(outcome, path, Map.of());
    }

    public static Map<String, Object> findPayload(AuditOutcome outcome, AuditPath path, Map<String, Object> details) {
        return envelope(outcome, path, details);
    }

    public static Map<String, Object> createPayload(AuditOutcome outcome, AuditPath path, Map<String, Object> details) {
        return envelope(outcome, path, details);
    }

    private static Map<String, Object> envelope(AuditOutcome outcome, AuditPath path, Map<String, Object> details) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("outcome", outcome);
        payload.put("path", path);
        payload.put("details", details == null ? Map.of() : details);
        return payload;
    }
}
