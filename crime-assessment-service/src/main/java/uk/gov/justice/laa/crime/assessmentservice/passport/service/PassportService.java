package uk.gov.justice.laa.crime.assessmentservice.passport.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.common.api.client.MaatDataApiClient;
import uk.gov.justice.laa.crime.common.model.passported.ApiCreatePassportedAssessmentRequest;
import uk.gov.justice.laa.crime.common.model.passported.ApiCreatePassportedAssessmentResponse;
import uk.gov.justice.laa.crime.common.model.passported.ApiGetPassportedAssessmentResponse;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassportService {
    private final MaatDataApiClient maatDataApiClient;

    public Optional<ApiGetPassportedAssessmentResponse> find(int legacyId) {
        try {
            return Optional.ofNullable(maatDataApiClient.getPassportAssessment(legacyId));
        } catch (Exception e) {
            log.error("Error occurred while retrieving passport assessment", e);
            throw e;
        }
    }

    public ApiCreatePassportedAssessmentResponse create(ApiCreatePassportedAssessmentRequest request) {
        try {
            return maatDataApiClient.createPassportAssessment(request);
        } catch (Exception e) {
            log.error("Error occurred while creating passport assessment", e);
            throw e;
        }
    }
}
