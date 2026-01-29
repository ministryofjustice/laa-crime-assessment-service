package uk.gov.justice.laa.crime.assessmentservice.audit.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventRecorder;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventRequest;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.listener.AuditEventPublished;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpringAuditEventRecorder implements AuditEventRecorder {

    private final ApplicationEventPublisher publisher;

    @Override
    public void record(AuditEventRequest request) {
        try {
            publisher.publishEvent(new AuditEventPublished(request));
        } catch (Exception e) {
            log.warn(
                    "Audit event publish failed (non-blocking). domain={} eventType={} traceId={}",
                    request.domain(),
                    request.eventType(),
                    request.traceId(),
                    e);
        }
    }
}
