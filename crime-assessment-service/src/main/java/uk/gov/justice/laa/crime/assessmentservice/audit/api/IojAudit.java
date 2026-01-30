package uk.gov.justice.laa.crime.assessmentservice.audit.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.mapper.AuditPayloads;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.mapper.IojAuditPayloadMapper;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IojAudit {

    private final AuditEventRecorder audit;
    private final TraceIdHandler traceIdHandler;
    private final TriggeredByResolver triggeredByResolver;

    public void recordFindByAppealId(UUID appealId, boolean found) {
        Map<String, Object> details = IojAuditPayloadMapper.createFindDetails(appealId);

        if (found) {
            audit.record(AuditRequests.findIojByAppealId(
                    appealId,
                    triggeredByResolver.resolve(),
                    traceIdHandler.getTraceId(),
                    AuditPayloads.findPayload(AuditOutcome.SUCCESS, AuditPath.LOCAL_HIT, details)));
        } else {
            // Key rule: on NOT_FOUND, don't include APPEAL_ID identifier (so FK column stays null),
            // but do include requestedId in payload details.
            audit.record(AuditRequests.findIojNotFoundByAppealId(
                    triggeredByResolver.resolve(),
                    traceIdHandler.getTraceId(),
                    AuditPayloads.findPayload(AuditOutcome.NOT_FOUND, AuditPath.LOCAL_MISS, details)));
        }
    }

    public void recordFindByLegacyIdHit(int legacyAppealId) {
        audit.record(AuditRequests.findIojByLegacyId(
                legacyAppealId,
                triggeredByResolver.resolve(),
                traceIdHandler.getTraceId(),
                AuditPayloads.findPayload(AuditOutcome.SUCCESS, AuditPath.LOCAL_HIT)));
    }

    public void recordFindByLegacyIdMissThenLegacyResult(int legacyAppealId, boolean legacyFound) {
        audit.record(AuditRequests.findIojByLegacyId(
                legacyAppealId,
                triggeredByResolver.resolve(),
                traceIdHandler.getTraceId(),
                AuditPayloads.findPayload(
                        legacyFound ? AuditOutcome.SUCCESS : AuditOutcome.NOT_FOUND,
                        legacyFound ? AuditPath.LOCAL_MISS_LEGACY_HIT : AuditPath.LOCAL_MISS_LEGACY_MISS)));
    }

    public void recordFindByLegacyIdLegacyFailure(int legacyAppealId, Exception e) {
        log.error("Error occurred fetching from legacy system for legacyAppealId: {}", legacyAppealId, e);

        audit.record(AuditRequests.findIojByLegacyId(
                legacyAppealId,
                triggeredByResolver.resolve(),
                traceIdHandler.getTraceId(),
                AuditPayloads.findPayload(AuditOutcome.FAILURE, AuditPath.LOCAL_MISS_LEGACY_FAILURE)));
    }

    public void recordCreateSuccess(UUID appealId, int legacyAppealId, ApiCreateIojAppealRequest request) {
        Map<String, Object> details = IojAuditPayloadMapper.createDetails(request);

        audit.record(AuditRequests.createIoj(
                appealId,
                legacyAppealId,
                triggeredByResolver.resolve(),
                traceIdHandler.getTraceId(),
                AuditPayloads.createPayload(AuditOutcome.SUCCESS, AuditPath.DUAL_WRITE_SUCCESS, details)));
    }

    public void recordCreateFailure(UUID appealId, int legacyAppealId, ApiCreateIojAppealRequest request, Exception e) {
        log.error("Failed to update local appeal with legacyAppealId", e);

        Map<String, Object> details = IojAuditPayloadMapper.createDetails(request);

        audit.record(AuditRequests.createIoj(
                appealId,
                legacyAppealId,
                triggeredByResolver.resolve(),
                traceIdHandler.getTraceId(),
                AuditPayloads.createPayload(AuditOutcome.FAILURE, AuditPath.DUAL_WRITE_FAILURE, details)));
    }
}
