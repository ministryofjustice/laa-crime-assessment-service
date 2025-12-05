package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        // set the legacy id on the new DB entity, and save.
        appealEntity.setLegacyAppealId(legacyAppeal.getLegacyAppealId());
        // TODO: Awaiting further rollback work here.
        //  If error below, should we rollback the above?
        //  If so, need MAAT endpoint.
        try {
            iojAppealService.save(appealEntity);
        } catch (Exception e) {
            log.error("Exception was hit during the second DB hit. This needs investigating.");
        }
        return appealEntity;
    }
}
