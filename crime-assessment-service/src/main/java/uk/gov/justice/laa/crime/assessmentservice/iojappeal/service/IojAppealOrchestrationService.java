package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.IojAuditRecorder;
import uk.gov.justice.laa.crime.assessmentservice.common.api.exception.AssessmentRollbackException;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.config.IojAppealMigrationProperties;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.dto.ApiRollbackIojAppealRequest;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

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

    @Transactional
    public ApiCreateIojAppealResponse createIojAppeal(ApiCreateIojAppealRequest request) {
        IojAppealEntity appealEntity = iojAppealService.create(request);
        ApiCreateIojAppealResponse legacyAppeal = legacyIojAppealService.create(request);
        Integer legacyAppealId = legacyAppeal.getLegacyAppealId();

        try {
            appealEntity.setLegacyAppealId(legacyAppealId);
            iojAppealService.save(appealEntity);
            iojAuditRecorder.recordCreateSuccess(appealEntity.getAppealId(), legacyAppeal.getLegacyAppealId(), request);
        } catch (Exception e) {
            legacyIojAppealService.rollback(legacyAppealId);
            iojAppealService.delete(appealEntity);
            iojAuditRecorder.recordCreateFailure(
                    appealEntity.getAppealId(), legacyAppeal.getLegacyAppealId(), request, e);
            throw new AssessmentRollbackException(String.format(
                    "Error linking appealId %s to legacyAppealId %d, creation has been rolled back: %s",
                    appealEntity.getAppealId().toString(), legacyAppealId, e.getMessage()));
        }

        return new ApiCreateIojAppealResponse()
                .withAppealId(appealEntity.getAppealId().toString())
                .withLegacyAppealId(legacyAppealId);
    }

    @Transactional
    public boolean rollbackIojAppeal(ApiRollbackIojAppealRequest request) {
        try {
            // TODO: Call a presumably new service here for creating a new record in the events table

            legacyIojAppealService.rollback(request.getLegacyAppealId());
        } catch (Exception ex) {
            // TODO: Log the error
            return false;
        }

        return true;
    }
}
