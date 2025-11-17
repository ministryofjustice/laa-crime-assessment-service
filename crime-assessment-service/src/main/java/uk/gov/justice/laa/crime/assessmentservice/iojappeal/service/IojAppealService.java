package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper.IojAppealMapper;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository.IojAppealRepository;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IojAppealService {

    private final IojAppealMapper iojAppealMapper;

    private final IojAppealRepository iojAppealRepository;

    public ApiGetIojAppealResponse findIojAppeal(UUID appealId) {
        IojAppealEntity entity = iojAppealRepository.findIojAppealByAppealId(appealId);

        if (entity == null) {
            return null;
        }

        return iojAppealMapper.mapEntityToDTO(entity);
    }
}
