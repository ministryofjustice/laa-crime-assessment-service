package uk.gov.justice.laa.crime.assessmentservice.audit.internal.service;

import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventRequest;

public interface AuditEventPersister {
    void persist(AuditEventRequest request);
}
