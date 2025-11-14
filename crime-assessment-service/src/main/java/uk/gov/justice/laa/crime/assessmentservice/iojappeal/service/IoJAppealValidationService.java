package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

public class IoJAppealValidationService {

    public void validate(ApiCreateIojAppealRequest request) {

        // null check on legacyApplicationId
        // check the appeal reason is of type HARDIOJ
        // check the legacyApplicationId is on the database
    }

    private void validateAppealReason() {
        // TODO: Add validation once crime-commons has been unblocked
    }

    private void validateLegacyApplicationId(int legacyApplicationId) {

    }

}
