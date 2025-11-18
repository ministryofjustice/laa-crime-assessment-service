package uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator;

import java.util.List;

import org.junit.jupiter.api.Test;

class ApiCreateIojAppealRequestValidatorTest {

    @Test
    void whenRequestEmpty_thenTwoErrors() {
        List<String> returnedErrorList =
                ApiCreateIojAppealRequestValidator.validateRequest(new ApiCreateIojAppealRequest());
    }
}
