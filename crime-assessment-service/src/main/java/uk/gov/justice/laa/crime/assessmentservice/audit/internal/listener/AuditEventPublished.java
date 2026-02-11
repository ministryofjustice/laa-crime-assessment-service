package uk.gov.justice.laa.crime.assessmentservice.audit.internal.listener;

import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventRequest;

public record AuditEventPublished(AuditEventRequest request) {}
