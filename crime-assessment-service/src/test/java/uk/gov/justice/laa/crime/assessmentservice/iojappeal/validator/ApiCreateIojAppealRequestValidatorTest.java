package uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.crime.common.model.common.ApiUserSession;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppealMetadata;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.IojAppealDecision;
import uk.gov.justice.laa.crime.enums.IojAppealDecisionReason;
import uk.gov.justice.laa.crime.enums.NewWorkReason;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class ApiCreateIojAppealRequestValidatorTest {

    // Null check validation.

    @Test
    void whenRequestEmpty_thenTwoErrors() {
        List<String> returnedErrorList =
                ApiCreateIojAppealRequestValidator.validateRequest(new ApiCreateIojAppealRequest());
        assertThat(returnedErrorList).hasSize(2);
        assertThat(returnedErrorList.stream()
                        .filter(x -> x.contains("is missing."))
                        .count())
                .isEqualTo(2);
    }

    @Test
    void whenAppealAndMetaPresentButNotPopulated_thenOnlyMissingErrors() {
        ApiCreateIojAppealRequest request = new ApiCreateIojAppealRequest();
        request.setIojAppeal(new IojAppeal());
        request.setIojAppealMetadata(new IojAppealMetadata());
        List<String> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        assertThat(returnedErrorList).hasSize(9); // 6 field validations on appeal, 3 on metadata.
        assertThat(returnedErrorList.stream()
                        .filter(x -> x.contains("is missing."))
                        .count())
                .isEqualTo(9);
        // ensure that the applicationId/LegacyApplicationId check has failed.
        assertThat(returnedErrorList.stream()
                        .filter(x -> x.equals("Both Application Id and Legacy Application Id is missing."))
                        .count())
                .isEqualTo(1);
    }

    // Appeal Reason/Assessor Combination Tests
    private static Stream<Arguments> reasonAssessorCombinations() {
        return Stream.of(
                Arguments.of(IojAppealAssessor.CASEWORKER, NewWorkReason.JR, false ),
                Arguments.of(IojAppealAssessor.CASEWORKER, NewWorkReason.NEW, true),
                Arguments.of(IojAppealAssessor.CASEWORKER, NewWorkReason.PRI, true),
                Arguments.of(IojAppealAssessor.JUDGE, NewWorkReason.JR, true),
                Arguments.of(IojAppealAssessor.JUDGE, NewWorkReason.NEW, false),
                Arguments.of(IojAppealAssessor.JUDGE, NewWorkReason.PRI, true)
        );
    }

    // Judicial Review Combinations
    @ParameterizedTest
    @MethodSource("reasonAssessorCombinations")
    void whenCombinationIsTestedAndInvalid_thenErrorShouldSurface(IojAppealAssessor assessor, NewWorkReason reason, boolean isValidCombination) {
        var request = createPopulatedValidRequest();
        request.getIojAppeal().setAppealAssessor(assessor);
        request.getIojAppeal().setAppealReason(reason);

        List<String> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        if(isValidCombination) {
            assertThat(returnedErrorList).isEmpty();
        }
        else {
            assertThat(returnedErrorList).hasSize(1);
            assertThat(returnedErrorList.getFirst()).isEqualTo("Incorrect Combination of Assessor and Reason.");
        }
    }

    @ParameterizedTest
    @EnumSource(value=NewWorkReason.class, names={"PRI", "NEW", "JR"},mode=EnumSource.Mode.EXCLUDE)
    void whenInvalidAppealReasonSelected_thenOneError(NewWorkReason reason) {
        // Judicial Review + Judge
        var request = createPopulatedValidRequest();
        request.getIojAppeal().setAppealReason(reason);

        List<String> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        assertThat(returnedErrorList).hasSize(1);
        assertThat(returnedErrorList.getFirst()).isEqualTo("Appeal Reason is invalid.");
    }

    // helpers
    private ApiCreateIojAppealRequest createPopulatedValidRequest() {
        ApiCreateIojAppealRequest request = new ApiCreateIojAppealRequest();
        var appeal = new IojAppeal();
        appeal.setAppealDecision(IojAppealDecision.PASS);
        appeal.setAppealReason(NewWorkReason.JR);
        appeal.setAppealAssessor(IojAppealAssessor.JUDGE);
        appeal.setDecisionReason(IojAppealDecisionReason.INTERESTS_PERSON);
        appeal.setReceivedDate(LocalDateTime.now());
        appeal.setDecisionDate(LocalDateTime.now());

        var metaData = new IojAppealMetadata();
        metaData.setApplicationId("123");
        metaData.setLegacyApplicationId(456);
        metaData.setUserSession(new ApiUserSession());
        metaData.setCaseManagementUnitId(789);

        request.setIojAppeal(appeal);
        request.setIojAppealMetadata(metaData);
        return request;
    }
}
