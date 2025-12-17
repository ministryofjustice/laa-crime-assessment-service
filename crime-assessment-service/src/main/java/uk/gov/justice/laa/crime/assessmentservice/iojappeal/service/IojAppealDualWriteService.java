package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.common.exception.AssessmentServiceException;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IojAppealDualWriteService {

    private final IojAppealService iojAppealService;
    private final LegacyIojAppealService legacyIojAppealService;

    @Transactional
    public IojAppealEntity createIojAppeal(ApiCreateIojAppealRequest request) {
        IojAppealEntity appealEntity = iojAppealService.create(request);
        ApiCreateIojAppealResponse legacyAppeal = legacyIojAppealService.create(request);
        Integer legacyAppealId = legacyAppeal.getLegacyAppealId();

        try {
            appealEntity.setLegacyAppealId(legacyAppealId);
            iojAppealService.save(appealEntity);
        } catch (Exception exc) {
            log.error("Error encountered linking appealId %d to legacyAppealId %d",
                appealEntity.getAppealId(), legacyAppealId);
            legacyIojAppealService.rollback(legacyAppealId);
            iojAppealService.delete(appealEntity);
            throw new AssessmentServiceException(String.format(
                "Exception encountered during linking process, creation has been rolled back: %s",
                exc.getMessage()));
        }

        return appealEntity;
    }
}
