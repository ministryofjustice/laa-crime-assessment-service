package uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator;

import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.crime.common.model.common.ApiUserSession;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppealMetadata;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.NewWorkReason;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.util.ObjectUtils;

@UtilityClass
public class ApiCreateIojAppealRequestValidator {
    private static final String FIELD_IS_MISSING = "%s is missing.";

    public List<String> validateRequest(ApiCreateIojAppealRequest request) {
        var errorList = new ArrayList<String>();
        if (Objects.isNull(request)) {
            return List.of(getMissingFieldErrorText("Request"));
        }

        validateMetaData(request.getIojAppealMetadata(), errorList);
        validateIoJAppeal(request.getIojAppeal(), errorList);

        return errorList;
    }

    // MetaData Validation

    void validateMetaData(IojAppealMetadata metadata, List<String> errorList) {
        if (Objects.isNull(metadata)) {
            errorList.add(getMissingFieldErrorText("Metadata"));
            return;
        }
        validateFieldNotEmpty(metadata.getCaseManagementUnitId(), "Case Management Unit", errorList);
        validateOneApplicationIdPresent(metadata, errorList);
        validateUserSession(metadata.getUserSession(), errorList);
    }

    // Ensure that we have an application ID. Either LegacyApplicationId, or ApplicationId must be present.
    private void validateOneApplicationIdPresent(IojAppealMetadata metadata, List<String> errorList) {
        if (Objects.isNull(metadata.getApplicationId()) && Objects.isNull(metadata.getLegacyApplicationId())) {
            errorList.add(getMissingFieldErrorText("Both Application Id and Legacy Application Id"));
        }
    }

    // ensure the username/session id are present in usersession.
    private void validateUserSession(ApiUserSession userSession, List<String> errorList) {
        if (Objects.isNull(userSession)) {
            errorList.add(getMissingFieldErrorText("User Session"));
        } else {
            validateFieldNotEmpty(userSession.getSessionId(), "Session ID", errorList);
            validateFieldNotEmpty(userSession.getUserName(), "Username", errorList);
        }
    }

    // Appeal Validation

    void validateIoJAppeal(IojAppeal appeal, List<String> errorList) {
        if (Objects.isNull(appeal)) {
            errorList.add(getMissingFieldErrorText("Appeal"));
            return;
        }
        validateFieldNotEmpty(appeal.getAppealDecision(), "Appeal Decision", errorList);
        validateFieldNotEmpty(appeal.getAppealAssessor(), "Appeal Assessor", errorList);
        validateFieldNotEmpty(appeal.getAppealReason(), "Appeal Reason", errorList);
        validateFieldNotEmpty(appeal.getDecisionReason(), "Decision Reason", errorList);
        validateFieldNotEmpty(appeal.getDecisionDate(), "Decision Date", errorList);
        validateFieldNotEmpty(appeal.getReceivedDate(), "Received Date", errorList);

        validateAppealReasonType(appeal.getAppealReason(), errorList);
        validateAppealReasonAppealAssessorCombinations(appeal, errorList);
    }

    // check the appeal reason is of type HARDIOJ
    private void validateAppealReasonType(NewWorkReason appealReason, List<String> errorList) {
        if (Objects.nonNull(appealReason) && !appealReason.getType().equalsIgnoreCase("HARDIOJ")) {
            errorList.add("Appeal Reason is invalid.");
        }
    }

    // check appeal reason/appeal accessor combination is valid.
    private void validateAppealReasonAppealAssessorCombinations(IojAppeal appeal, List<String> errorList) {
        NewWorkReason reason = appeal.getAppealReason();
        IojAppealAssessor assessor = appeal.getAppealAssessor();
        // Null check Assessor as != includes nulls.
        if ((Objects.nonNull(assessor))
                && (NewWorkReason.getFrom("NEW").equals(reason) && (!IojAppealAssessor.CASEWORKER.equals(assessor))
                        || (NewWorkReason.getFrom("JR").equals(reason) && !IojAppealAssessor.JUDGE.equals(assessor)))) {
            errorList.add("Incorrect Combination of Assessor and Reason.");
        }
    }

    // Utility Methods
    private void validateFieldNotEmpty(Object field, String fieldName, List<String> errorList) {
        if (ObjectUtils.isEmpty(field)) {
            errorList.add(getMissingFieldErrorText(fieldName));
        }
    }

    private String getMissingFieldErrorText(String fieldName) {
        return String.format(FIELD_IS_MISSING, fieldName);
    }
}
