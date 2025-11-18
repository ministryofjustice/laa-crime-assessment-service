package uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator;

import static org.assertj.core.api.Assertions.assertThat;

import uk.gov.justice.laa.crime.common.model.common.ApiUserSession;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.IojAppealDecision;
import uk.gov.justice.laa.crime.enums.IojAppealDecisionReason;
import uk.gov.justice.laa.crime.enums.NewWorkReason;

import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    void whenAllFieldsFilledWithInvalidAppealReason_thenSingleErrorIsAssessor() {
        // Judicial Review + No Judge
        var request = createPopulatedValidRequest();
        request.getIojAppeal().setAppealReason(NewWorkReason.CPS);

        List<String> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        assertThat(returnedErrorList).hasSize(1);
        assertThat(returnedErrorList.getFirst()).isEqualTo("Appeal Reason is invalid.");
    }

    // Appeal Reason/Assessor Combination Tests

    // Judicial Review Combinations

    @Test
    void whenAllFieldsFilledWithJRandCaseworker_thenSingleErrorIsAssessor() {
        // Judicial Review + No Judge
        var request = createPopulatedValidRequest();
        request.getIojAppeal().setAppealAssessor(IojAppealAssessor.CASEWORKER);
        request.getIojAppeal().setAppealReason(NewWorkReason.JR);

        List<String> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        assertThat(returnedErrorList).hasSize(1);
        assertThat(returnedErrorList.getFirst()).isEqualTo("Incorrect Combination of Assessor and Reason.");
    }

    @Test
    void whenAllFieldsFilledWithJRandJudge_thenNoErrors() {
        // Judicial Review + Judge
        var request = createPopulatedValidRequest();
        request.getIojAppeal().setAppealAssessor(IojAppealAssessor.JUDGE);
        request.getIojAppeal().setAppealReason(NewWorkReason.JR);

        List<String> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        assertThat(returnedErrorList).isEmpty();
    }

    // New Appeal Combinations

    @Test
    void whenAllFieldsFilledWithNewandJudge_thenSingleErrorIsAssessor() {
        // Judicial Review + No Judge
        var request = createPopulatedValidRequest();
        request.getIojAppeal().setAppealAssessor(IojAppealAssessor.JUDGE);
        request.getIojAppeal().setAppealReason(NewWorkReason.NEW);

        List<String> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        assertThat(returnedErrorList).hasSize(1);
        assertThat(returnedErrorList.getFirst()).isEqualTo("Incorrect Combination of Assessor and Reason.");
    }

    @Test
    void whenAllFieldsFilledWithNewandCaseworker_thenNoErrors() {
        // Judicial Review + Judge
        var request = createPopulatedValidRequest();
        request.getIojAppeal().setAppealAssessor(IojAppealAssessor.CASEWORKER);
        request.getIojAppeal().setAppealReason(NewWorkReason.NEW);

        List<String> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        assertThat(returnedErrorList).isEmpty();
    }

    // Previous Review Invalid Combinations

    @Test
    void whenAllFieldsFilledWithPRIandCaseworker_thenNoErrors() {
        // Judicial Review + Judge
        var request = createPopulatedValidRequest();
        request.getIojAppeal().setAppealAssessor(IojAppealAssessor.CASEWORKER);
        request.getIojAppeal().setAppealReason(NewWorkReason.PRI);

        List<String> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        assertThat(returnedErrorList).isEmpty();
    }

    @Test
    void whenAllFieldsFilledWithPRIandJudge_thenNoErrors() {
        // Judicial Review + Judge
        var request = createPopulatedValidRequest();
        request.getIojAppeal().setAppealAssessor(IojAppealAssessor.JUDGE);
        request.getIojAppeal().setAppealReason(NewWorkReason.PRI);

        List<String> returnedErrorList = ApiCreateIojAppealRequestValidator.validateRequest(request);
        assertThat(returnedErrorList).isEmpty();
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
