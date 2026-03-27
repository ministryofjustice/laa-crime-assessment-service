package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.IojAuditRecorder;
import uk.gov.justice.laa.crime.assessmentservice.common.api.exception.AssessmentCreateException;
import uk.gov.justice.laa.crime.assessmentservice.common.api.exception.RequestedObjectNotFoundException;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.config.IojAppealMigrationProperties;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiRollbackIojAppealResponse;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IojAppealOrchestrationService {

    private final IojAuditRecorder iojAuditRecorder;
    private final IojAppealService iojAppealService;
    private final LegacyIojAppealService legacyIojAppealService;
    private final IojAppealMigrationProperties migrationProperties;

    public ApiGetIojAppealResponse findOrThrow(UUID appealId) {
        return find(appealId)
                .orElseThrow(
                        () -> new RequestedObjectNotFoundException("IOJ appeal not found for appealId: " + appealId));
    }

    public ApiGetIojAppealResponse findOrThrow(int legacyAppealId) {
        return find(legacyAppealId)
                .orElseThrow(() -> new RequestedObjectNotFoundException(
                        "IOJ appeal not found for legacyAppealId: " + legacyAppealId));
    }

    public Optional<ApiGetIojAppealResponse> find(UUID appealId) {
        Optional<ApiGetIojAppealResponse> local = iojAppealService.find(appealId);
        iojAuditRecorder.recordFindByAppealId(appealId, local.isPresent());
        return local;
    }

    public Optional<ApiGetIojAppealResponse> find(int legacyAppealId) {
        Optional<ApiGetIojAppealResponse> local = iojAppealService.find(legacyAppealId);

        if (local.isPresent()) {
            iojAuditRecorder.recordFindByLegacyId(legacyAppealId, true);
            return local;
        }

        if (iojAppealService.hasBeenRolledBack(legacyAppealId)) {
            iojAuditRecorder.recordFindByLegacyId(legacyAppealId, false);
            return Optional.empty();
        }

        if (!migrationProperties.legacyReadFallbackEnabled()) {
            iojAuditRecorder.recordFindByLegacyId(legacyAppealId, false);
            return Optional.empty();
        }

        return findInLegacyAfterLocalMiss(legacyAppealId);
    }

    private Optional<ApiGetIojAppealResponse> findInLegacyAfterLocalMiss(int legacyAppealId) {
        try {
            Optional<ApiGetIojAppealResponse> legacy = legacyIojAppealService.find(legacyAppealId);
            iojAuditRecorder.recordFindByLegacyIdMissThenLegacyResult(legacyAppealId, legacy.isPresent());
            return legacy;
        } catch (Exception e) {
            iojAuditRecorder.recordFindByLegacyIdLegacyFailure(legacyAppealId, e);
            throw e;
        }
    }

    public ApiCreateIojAppealResponse createIojAppeal(ApiCreateIojAppealRequest request) {
        IojAppealEntity appealEntity;
        try {
            appealEntity = iojAppealService.create(request);
        } catch (Exception e) {
            throw new AssessmentCreateException(String.format("Error creating initial IojAppeal: %s", e.getMessage()));
        }
        createAndLinkLegacyAppeal(request, appealEntity);
        return new ApiCreateIojAppealResponse()
                .withAppealId(appealEntity.getAppealId().toString())
                .withLegacyAppealId(appealEntity.getLegacyAppealId());
    }

    public void createAndLinkLegacyAppeal(ApiCreateIojAppealRequest request, IojAppealEntity appealEntity) {
        Integer legacyAppealId = null;
        try {
            legacyAppealId = legacyIojAppealService.create(request).getLegacyAppealId();
            appealEntity.setLegacyAppealId(legacyAppealId);
            iojAppealService.save(appealEntity);
            iojAuditRecorder.recordCreateSuccess(appealEntity.getAppealId(), appealEntity.getLegacyAppealId(), request);
        } catch (Exception e) {
            if (legacyAppealId != null) {
                legacyIojAppealService.rollback(legacyAppealId);
            }
            iojAppealService.markRolledBack(appealEntity);
            iojAuditRecorder.recordCreateFailure(appealEntity.getAppealId(), legacyAppealId, request, e);
            throw new AssessmentCreateException(String.format(
                    "Error linking appealId %s to legacyAppealId %d, creation has been rolled back: %s",
                    appealEntity.getAppealId().toString(), legacyAppealId, e.getMessage()));
        }
    }

    @Transactional
    public ApiRollbackIojAppealResponse rollbackIojAppeal(UUID appealId) {
        IojAppealEntity appeal = iojAppealService
                .findEntity(appealId)
                .orElseThrow(
                        () -> new RequestedObjectNotFoundException("IOJ appeal not found for appealId: " + appealId));

        if (appeal.isRolledBack()) {
            return new ApiRollbackIojAppealResponse()
                    .withAppealId(appeal.getAppealId().toString())
                    .withLegacyAppealId(appeal.getLegacyAppealId())
                    .withRollbackSuccessful(true);
        }

        boolean rollbackSuccessful;

        try {
            legacyIojAppealService.rollback(appeal.getLegacyAppealId());
            iojAppealService.markRolledBack(appeal);

            iojAuditRecorder.recordRollbackSuccess(appeal.getAppealId(), appeal.getLegacyAppealId());

            rollbackSuccessful = true;

        } catch (Exception ex) {
            // We can't rollback the rollback attempt, so just log the failure.
            iojAuditRecorder.recordRollbackFailure(appealId, appeal.getLegacyAppealId(), ex);

            rollbackSuccessful = false;
        }

        return new ApiRollbackIojAppealResponse()
                .withAppealId(appeal.getAppealId().toString())
                .withLegacyAppealId(appeal.getLegacyAppealId())
                .withRollbackSuccessful(rollbackSuccessful);
    }
}
