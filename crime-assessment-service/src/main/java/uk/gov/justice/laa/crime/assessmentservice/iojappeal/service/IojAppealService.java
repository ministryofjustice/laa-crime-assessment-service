package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper.IojAppealMapper;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository.IojAppealRepository;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IojAppealService {

    private final IojAppealMapper iojAppealMapper;
    private final IojAppealRepository iojAppealRepository;

    public Optional<IojAppealEntity> findEntity(UUID appealId) {
        return iojAppealRepository.findByAppealId(appealId);
    }

    public Optional<ApiGetIojAppealResponse> find(UUID appealId) {
        return iojAppealRepository.findByAppealIdAndRolledBackAtIsNull(appealId).map(iojAppealMapper::mapEntityToDTO);
    }

    public Optional<ApiGetIojAppealResponse> find(int legacyAppealId) {
        return iojAppealRepository
                .findByLegacyAppealIdAndRolledBackAtIsNull(legacyAppealId)
                .map(iojAppealMapper::mapEntityToDTO);
    }

    public boolean hasBeenRolledBack(int legacyAppealId) {
        return iojAppealRepository.existsByLegacyAppealIdAndRolledBackAtIsNotNull(legacyAppealId);
    }

    public IojAppealEntity create(ApiCreateIojAppealRequest request) {
        IojAppealEntity iojAppealEntity = iojAppealMapper.mapCreateAppealRequestToEntity(request);
        iojAppealRepository.save(iojAppealEntity);
        return iojAppealEntity;
    }

    public IojAppealEntity save(IojAppealEntity iojAppealEntity) {
        return iojAppealRepository.save(iojAppealEntity);
    }

    public void delete(IojAppealEntity iojAppealEntity) {
        iojAppealRepository.delete(iojAppealEntity);
    }

    public void markRolledBack(IojAppealEntity entity) {
        entity.setRolledBackAt(Instant.now());
        iojAppealRepository.save(entity);
    }
}
