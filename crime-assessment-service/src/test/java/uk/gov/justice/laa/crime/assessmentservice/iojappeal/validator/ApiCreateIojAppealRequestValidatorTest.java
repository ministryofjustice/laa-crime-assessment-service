package uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.APPEAL_SUCCESSFUL;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.APPLICATION_RECEIVED_DATE;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.LEGACY_APPLICATION_ID;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator.ApiCreateIojAppealRequestValidator.ERROR_APPEAL_REASON_IS_INVALID;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator.ApiCreateIojAppealRequestValidator.ERROR_FIELD_IS_MISSING;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator.ApiCreateIojAppealRequestValidator.ERROR_INCORRECT_COMBINATION;

import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.NewWorkReason;
import uk.gov.justice.laa.crime.error.ErrorMessage;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class ApiCreateIojAppealRequestValidatorTest {

    // Appeal Reason/Assessor Combination Tests
    private static Stream<Arguments> reasonAssessorCombinations() {
        return Stream.of(
                Arguments.of(IojAppealAssessor.CASEWORKER, NewWorkReason.JR, false),
                Arguments.of(IojAppealAssessor.CASEWORKER, NewWorkReason.NEW, true),
                Arguments.of(IojAppealAssessor.CASEWORKER, NewWorkReason.PRI, true),
                Arguments.of(IojAppealAssessor.JUDGE, NewWorkReason.JR, true),
                Arguments.of(IojAppealAssessor.JUDGE, NewWorkReason.NEW, false),
                Arguments.of(IojAppealAssessor.JUDGE, NewWorkReason.PRI, true));
    }

    @ParameterizedTest
    @MethodSource("reasonAssessorCombinations")
    void whenIojAssessorAndReasonCombinationIsTested_thenErrorShouldSurfaceIfInvalidCombination(
            IojAppealAssessor assessor, NewWorkReason reason, boolean isValidCombination) {
        ApiCreateIojAppealRequest request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        request.getIojAppeal().setAppealAssessor(assessor);
        request.getIojAppeal().setAppealReason(reason);

        List<ErrorMessage> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        if (isValidCombination) {
            assertThat(returnedErrorList).isEmpty();
        } else {
            assertThat(returnedErrorList).hasSize(1);
            assertThat(returnedErrorList.getFirst().message()).isEqualTo(ERROR_INCORRECT_COMBINATION);
        }
    }

    @ParameterizedTest
    @EnumSource(
            value = NewWorkReason.class,
            names = {"PRI", "NEW", "JR"},
            mode = EnumSource.Mode.EXCLUDE)
    void whenInvalidAppealReasonSelected_thenOneError(NewWorkReason reason) {
        // Judicial Review + Judge
        ApiCreateIojAppealRequest request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        request.getIojAppeal().setAppealReason(reason);

        List<ErrorMessage> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        assertThat(returnedErrorList).hasSize(1);
        assertThat(returnedErrorList.getFirst().message()).isEqualTo(ERROR_APPEAL_REASON_IS_INVALID);
    }

    @Test
    void givenMissingLegacyApplicationId_whenValidateRequest_thenReturnsMissingFieldError() {
        ApiCreateIojAppealRequest request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        request.getIojAppealMetadata().setLegacyApplicationId(null);

        List<ErrorMessage> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);

        assertThat(returnedErrorList).hasSize(1);
        assertThat(returnedErrorList.getFirst().field()).isEqualTo(LEGACY_APPLICATION_ID.getName());
        assertThat(returnedErrorList.getFirst().message())
                .isEqualTo(String.format(ERROR_FIELD_IS_MISSING, LEGACY_APPLICATION_ID.getName()));
    }

    @Test
    void givenMissingApplicationReceivedDate_whenValidateRequest_thenReturnsMissingFieldError() {
        ApiCreateIojAppealRequest request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        request.getIojAppealMetadata().setApplicationReceivedDate(null);

        List<ErrorMessage> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);

        assertThat(returnedErrorList).hasSize(1);
        assertThat(returnedErrorList.getFirst().field()).isEqualTo(APPLICATION_RECEIVED_DATE.getName());
        assertThat(returnedErrorList.getFirst().message())
                .isEqualTo(String.format(ERROR_FIELD_IS_MISSING, APPLICATION_RECEIVED_DATE.getName()));
    }

    @Test
    void givenMissingAppealSuccessful_whenValidateRequest_thenReturnsMissingFieldError() {
        ApiCreateIojAppealRequest request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        request.getIojAppeal().setAppealSuccessful(null);

        List<ErrorMessage> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);

        assertThat(returnedErrorList).hasSize(1);
        assertThat(returnedErrorList.getFirst().field()).isEqualTo(APPEAL_SUCCESSFUL.getName());
        assertThat(returnedErrorList.getFirst().message())
                .isEqualTo(String.format(ERROR_FIELD_IS_MISSING, APPEAL_SUCCESSFUL.getName()));
    }
}
