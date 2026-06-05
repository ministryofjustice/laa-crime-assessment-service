package uk.gov.justice.laa.crime.assessmentservice.passport.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.laa.crime.assessmentservice.passport.validator.ApiCreatePassportedAssessmentRequestValidator.ERROR_LAST_SIGN_ON_DATE_EMPTY;

import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;
import uk.gov.justice.laa.crime.common.model.passported.ApiCreatePassportedAssessmentRequest;
import uk.gov.justice.laa.crime.enums.BenefitType;
import uk.gov.justice.laa.crime.error.ErrorMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ApiCreatePassportedAssessmentRequestValidatorTest {
    private static Stream<Arguments> benefitTypeArguments() {
        return Stream.of(
                Arguments.of(BenefitType.JSA, null, false),
                Arguments.of(BenefitType.JSA, LocalDateTime.now(), true),
                Arguments.of(BenefitType.ESA, null, true));
    }

    @ParameterizedTest
    @MethodSource("benefitTypeArguments")
    void givenDeclaredBenefitAndLastSignOnDate_whenCombinationInvalid_thenErrorMessageIsReturned(
            BenefitType benefitType, LocalDateTime lastSignOnDate, boolean isValidCombination) {
        ApiCreatePassportedAssessmentRequest request =
                TestDataBuilder.buildValidPopulatedCreatePassportedAssessmentRequest();
        request.getPassportedAssessment().getDeclaredBenefit().setBenefitType(benefitType);
        request.getPassportedAssessment().getDeclaredBenefit().setLastSignOnDate(lastSignOnDate);

        List<ErrorMessage> returnedErrorList = ApiCreatePassportedAssessmentRequestValidator.validateRequest(request);
        if (isValidCombination) {
            assertThat(returnedErrorList).isEmpty();
        } else {
            assertThat(returnedErrorList).hasSize(1);
            assertThat(returnedErrorList.getFirst().message()).isEqualTo(ERROR_LAST_SIGN_ON_DATE_EMPTY);
        }
    }
}
