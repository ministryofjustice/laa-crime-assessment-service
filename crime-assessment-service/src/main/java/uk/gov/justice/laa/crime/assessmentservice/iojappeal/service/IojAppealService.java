package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper.IojAppealMapper;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository.IojAppealRepository;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IojAppealService {

    private final IojAppealMapper iojAppealMapper;

    private final IojAppealRepository iojAppealRepository;

    public Optional<ApiGetIojAppealResponse> find(UUID appealId) {
        return Optional.ofNullable(iojAppealRepository.findIojAppealByAppealId(appealId))
                .map(iojAppealMapper::mapEntityToDTO);
    }

    public IojAppealEntity createIojAppeal(ApiCreateIojAppealRequest request) {
        IojAppealEntity iojAppealEntity = iojAppealMapper.mapCreateAppealToEntity(request);
        iojAppealRepository.save(iojAppealEntity);
        return iojAppealEntity;
    }

    public IojAppealEntity saveIojAppeal(IojAppealEntity iojAppealEntity) {
        return iojAppealRepository.save(iojAppealEntity);
    }
}
