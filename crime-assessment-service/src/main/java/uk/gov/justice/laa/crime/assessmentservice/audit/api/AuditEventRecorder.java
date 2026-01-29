package uk.gov.justice.laa.crime.assessmentservice.audit.api;

public interface AuditEventRecorder {
    void record(AuditEventRequest request);
}
