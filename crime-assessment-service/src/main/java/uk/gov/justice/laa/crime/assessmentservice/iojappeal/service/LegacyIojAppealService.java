package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.common.client.MaatCourtDataApiClient;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper.IojAppealMapper;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository.IojAppealRepository;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegacyIojAppealService {

    private final IojAppealMapper iojAppealMapper;

    private final IojAppealRepository iojAppealRepository;

    private final MaatCourtDataApiClient maatCourtDataApiClient;

    public ApiGetIojAppealResponse find(int legacyAppealId) {
        IojAppealEntity entity = iojAppealRepository.findIojAppealByLegacyAppealId(legacyAppealId);

        if (entity != null) {
            return iojAppealMapper.mapEntityToDTO(entity);
        }

        return maatCourtDataApiClient.getIojAppeal(legacyAppealId);
    }
}
