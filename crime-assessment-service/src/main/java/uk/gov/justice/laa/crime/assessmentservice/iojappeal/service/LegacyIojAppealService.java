package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper.IojAppealMapper;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository.IojAppealRepository;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegacyIojAppealService {

    public ApiGetIojAppealResponse findIojAppeal(UUID appealId) {
        return null;
    }
}
