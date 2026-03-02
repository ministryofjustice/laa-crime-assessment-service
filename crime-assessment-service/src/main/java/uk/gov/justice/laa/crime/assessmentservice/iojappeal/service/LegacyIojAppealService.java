package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.common.api.client.MaatDataApiClient;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegacyIojAppealService {

    private final MaatDataApiClient maatDataApiClient;

    public Optional<ApiGetIojAppealResponse> find(int legacyAppealId) {
        return Optional.ofNullable(maatDataApiClient.getIojAppeal(legacyAppealId));
    }

    public ApiCreateIojAppealResponse create(ApiCreateIojAppealRequest request) {
        return maatDataApiClient.createIojAppeal(request);
    }

    public void rollback(int legacyAppealId) {
        maatDataApiClient.rollbackIojAppeal(legacyAppealId);
    }
}
