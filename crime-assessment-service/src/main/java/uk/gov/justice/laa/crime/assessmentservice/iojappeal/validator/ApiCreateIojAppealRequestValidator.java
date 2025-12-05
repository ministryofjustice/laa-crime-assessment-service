package uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator;

import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.APPEAL;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.APPEAL_ASSESSOR;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.APPEAL_REASON;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.APPEAL_SUCCESSFUL;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.APPLICATION_RECEIVED_DATE;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.CASE_MANAGEMENT_UNIT;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.DECISION_DATE;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.DECISION_REASON;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.LEGACY_APPLICATION_ID;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.METADATA;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.RECEIVED_DATE;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.REQUEST;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.SESSION_ID;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.USERNAME;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.USER_SESSION;

import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields;
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
    // error messages
    public static final String ERROR_FIELD_IS_MISSING = "%s is missing.";
    public static final String ERROR_INCORRECT_COMBINATION = "Incorrect Combination of Assessor and Reason.";
    public static final String ERROR_APPEAL_REASON_IS_INVALID = "Appeal Reason is invalid.";

    public static final String APPEAL_REASON_HARDIOJ = "HARDIOJ";

    public List<String> validateRequest(ApiCreateIojAppealRequest request) {
        var errorList = new ArrayList<String>();
        if (Objects.isNull(request)) {
            return List.of(getMissingFieldErrorText(REQUEST));
        }

        validateMetaData(request.getIojAppealMetadata(), errorList);
        validateIoJAppeal(request.getIojAppeal(), errorList);

        return errorList;
    }

    // MetaData Validation

    void validateMetaData(IojAppealMetadata metadata, List<String> errorList) {
        if (Objects.isNull(metadata)) {
            errorList.add(getMissingFieldErrorText(METADATA));
            return;
        }
        validateFieldNotEmpty(metadata.getCaseManagementUnitId(), CASE_MANAGEMENT_UNIT, errorList);
        validateFieldNotEmpty(metadata.getLegacyApplicationId(), LEGACY_APPLICATION_ID, errorList);
        validateFieldNotEmpty(metadata.getApplicationReceivedDate(), APPLICATION_RECEIVED_DATE, errorList);
        validateUserSession(metadata.getUserSession(), errorList);
    }

    // ensure the username/session id are present in usersession.
    private void validateUserSession(ApiUserSession userSession, List<String> errorList) {
        if (Objects.isNull(userSession)) {
            errorList.add(getMissingFieldErrorText(USER_SESSION));
        } else {
            validateFieldNotEmpty(userSession.getSessionId(), SESSION_ID, errorList);
            validateFieldNotEmpty(userSession.getUserName(), USERNAME, errorList);
        }
    }

    // Appeal Validation

    void validateIoJAppeal(IojAppeal appeal, List<String> errorList) {
        if (Objects.isNull(appeal)) {
            errorList.add(getMissingFieldErrorText(APPEAL));
            return;
        }
        validateFieldNotEmpty(appeal.getAppealSuccessful(), APPEAL_SUCCESSFUL, errorList);
        validateFieldNotEmpty(appeal.getAppealAssessor(), APPEAL_ASSESSOR, errorList);
        validateFieldNotEmpty(appeal.getAppealReason(), APPEAL_REASON, errorList);
        validateFieldNotEmpty(appeal.getDecisionReason(), DECISION_REASON, errorList);
        validateFieldNotEmpty(appeal.getDecisionDate(), DECISION_DATE, errorList);
        validateFieldNotEmpty(appeal.getReceivedDate(), RECEIVED_DATE, errorList);

        validateAppealReasonType(appeal.getAppealReason(), errorList);
        validateAppealReasonAppealAssessorCombinations(appeal, errorList);
    }

    // check the appeal reason is of type HARDIOJ
    private void validateAppealReasonType(NewWorkReason appealReason, List<String> errorList) {
        if (Objects.nonNull(appealReason) && !appealReason.getType().equalsIgnoreCase(APPEAL_REASON_HARDIOJ)) {
            errorList.add(ERROR_APPEAL_REASON_IS_INVALID);
        }
    }

    // check appeal reason/appeal accessor combination is valid.
    private void validateAppealReasonAppealAssessorCombinations(IojAppeal appeal, List<String> errorList) {
        NewWorkReason reason = appeal.getAppealReason();
        IojAppealAssessor assessor = appeal.getAppealAssessor();
        // Null check Assessor as != includes nulls.
        if ((Objects.nonNull(assessor))
                && (NewWorkReason.NEW.equals(reason) && (!IojAppealAssessor.CASEWORKER.equals(assessor))
                        || (NewWorkReason.JR.equals(reason) && !IojAppealAssessor.JUDGE.equals(assessor)))) {
            errorList.add(ERROR_INCORRECT_COMBINATION);
        }
    }

    // Utility Methods
    private void validateFieldNotEmpty(
            Object field, ApiCreateIojAppealRequestFields fieldName, List<String> errorList) {
        if (ObjectUtils.isEmpty(field)) {
            errorList.add(getMissingFieldErrorText(fieldName));
        }
    }

    private String getMissingFieldErrorText(ApiCreateIojAppealRequestFields fieldName) {
        return String.format(ERROR_FIELD_IS_MISSING, fieldName.getName());
    }
}
