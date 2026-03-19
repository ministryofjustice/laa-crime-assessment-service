package uk.gov.justice.laa.crime.assessmentservice.audit.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.helper.ClientIdResolver;
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
public class IojAuditRecorder {

    private final AuditEventRecorder audit;
    private final TraceIdHandler traceIdHandler;
    private final ClientIdResolver clientIdResolver;

    public void recordFindByAppealId(UUID appealId, boolean found) {
        Map<String, Object> details = IojAuditPayloadMapper.mapFindDetails(appealId);

        String traceId = traceIdHandler.getTraceId();
        String triggeredBy = clientIdResolver.resolveOrAnonymous();

        AuditPath path = found ? AuditPath.LOCAL_HIT : AuditPath.LOCAL_MISS;
        AuditOutcome outcome = found ? AuditOutcome.SUCCESS : AuditOutcome.NOT_FOUND;

        // On NOT_FOUND, don't include APPEAL_ID identifier (so FK column stays null)
        // but do include requestedId in payload details.
        UUID identifierAppealId = found ? appealId : null;

        audit.record(AuditRequests.findIojByAppealId(
                identifierAppealId, triggeredBy, traceId, AuditPayloads.findPayload(outcome, path, details)));
    }

    public void recordFindByLegacyId(int legacyAppealId, boolean found) {
        AuditPath path = found ? AuditPath.LOCAL_HIT : AuditPath.LOCAL_MISS;
        AuditOutcome outcome = found ? AuditOutcome.SUCCESS : AuditOutcome.NOT_FOUND;

        audit.record(AuditRequests.findIojByLegacyId(
                legacyAppealId,
                clientIdResolver.resolveOrAnonymous(),
                traceIdHandler.getTraceId(),
                AuditPayloads.findPayload(outcome, path)));
    }

    public void recordFindByLegacyIdMissThenLegacyResult(int legacyAppealId, boolean legacyFound) {
        audit.record(AuditRequests.findIojByLegacyId(
                legacyAppealId,
                clientIdResolver.resolveOrAnonymous(),
                traceIdHandler.getTraceId(),
                AuditPayloads.findPayload(
                        legacyFound ? AuditOutcome.SUCCESS : AuditOutcome.NOT_FOUND,
                        legacyFound ? AuditPath.LOCAL_MISS_LEGACY_HIT : AuditPath.LOCAL_MISS_LEGACY_MISS)));
    }

    public void recordFindByLegacyIdLegacyFailure(int legacyAppealId, Exception e) {
        log.error("Error occurred fetching from legacy system for legacyAppealId: {}", legacyAppealId, e);

        audit.record(AuditRequests.findIojByLegacyId(
                legacyAppealId,
                clientIdResolver.resolveOrAnonymous(),
                traceIdHandler.getTraceId(),
                AuditPayloads.findPayload(AuditOutcome.FAILURE, AuditPath.LOCAL_MISS_LEGACY_FAILURE)));
    }

    public void recordCreateSuccess(UUID appealId, int legacyAppealId, ApiCreateIojAppealRequest request) {
        Map<String, Object> details = IojAuditPayloadMapper.mapCreateDetails(request);

        audit.record(AuditRequests.createIoj(
                appealId,
                legacyAppealId,
                clientIdResolver.resolveOrAnonymous(),
                traceIdHandler.getTraceId(),
                AuditPayloads.createPayload(AuditOutcome.SUCCESS, AuditPath.DUAL_WRITE_SUCCESS, details)));
    }

    public void recordCreateFailure(UUID appealId, int legacyAppealId, ApiCreateIojAppealRequest request, Exception e) {
        log.error("Failed to update local appeal with legacyAppealId", e);

        Map<String, Object> details = IojAuditPayloadMapper.mapCreateDetails(request);

        audit.record(AuditRequests.createIoj(
                appealId,
                legacyAppealId,
                clientIdResolver.resolveOrAnonymous(),
                traceIdHandler.getTraceId(),
                AuditPayloads.createPayload(AuditOutcome.FAILURE, AuditPath.DUAL_WRITE_FAILURE, details)));
    }

    public void recordRollbackSuccess(UUID appealId, int legacyAppealId) {
        Map<String, Object> details = IojAuditPayloadMapper.mapRollbackDetails(appealId, legacyAppealId);

        audit.record(AuditRequests.rollbackIoj(
                appealId,
                legacyAppealId,
                clientIdResolver.resolveOrAnonymous(),
                traceIdHandler.getTraceId(),
                AuditPayloads.createPayload(AuditOutcome.SUCCESS, AuditPath.DUAL_WRITE_SUCCESS, details)));
    }

    public void recordRollbackFailure(UUID appealId, int legacyAppealId, Exception e) {
        log.error("Failed to rollback appeal with legacyAppealId", e);

        Map<String, Object> details = IojAuditPayloadMapper.mapRollbackDetails(appealId, legacyAppealId);

        audit.record(AuditRequests.rollbackIoj(
                appealId,
                legacyAppealId,
                clientIdResolver.resolveOrAnonymous(),
                traceIdHandler.getTraceId(),
                AuditPayloads.createPayload(AuditOutcome.FAILURE, AuditPath.DUAL_WRITE_FAILURE, details)));
    }
}
