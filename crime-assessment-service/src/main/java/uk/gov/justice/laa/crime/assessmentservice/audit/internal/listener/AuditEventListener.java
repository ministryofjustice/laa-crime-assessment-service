package uk.gov.justice.laa.crime.assessmentservice.audit.internal.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventRequest;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.service.IojAuditEventPersister;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final IojAuditEventPersister iojPersister;

    @Async
    @EventListener
    public void on(AuditEventPublished event) {
        AuditEventRequest request = event.request();

        try {
            switch (request.domain()) {
                case IOJ_APPEAL -> iojPersister.persist(request);
                default -> log.warn("Audit event ignored: unsupported domain={}", request.domain());
            }
        } catch (Exception e) {
            log.warn(
                    "Audit write failed (non-blocking). domain={} eventType={} traceId={}",
                    request.domain(),
                    request.eventType(),
                    request.traceId(),
                    e);
        }
    }
}
