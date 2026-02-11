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
import uk.gov.justice.laa.crime.error.ErrorMessage;

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

    public List<ErrorMessage> validateRequest(ApiCreateIojAppealRequest request) {
        var errorList = new ArrayList<ErrorMessage>();
        if (Objects.isNull(request)) {
            addMissingFieldError(REQUEST, errorList);
            return errorList;
        }

        validateMetaData(request.getIojAppealMetadata(), errorList);
        validateIoJAppeal(request.getIojAppeal(), errorList);

        return errorList;
    }

    // MetaData Validation

    void validateMetaData(IojAppealMetadata metadata, List<ErrorMessage> errorList) {
        if (Objects.isNull(metadata)) {
            addMissingFieldError(METADATA, errorList);
            return;
        }
        validateFieldNotEmpty(metadata.getCaseManagementUnitId(), CASE_MANAGEMENT_UNIT, errorList);
        validateFieldNotEmpty(metadata.getLegacyApplicationId(), LEGACY_APPLICATION_ID, errorList);
        validateFieldNotEmpty(metadata.getApplicationReceivedDate(), APPLICATION_RECEIVED_DATE, errorList);
        validateUserSession(metadata.getUserSession(), errorList);
    }

    // ensure the username/session id are present in usersession.
    private void validateUserSession(ApiUserSession userSession, List<ErrorMessage> errorList) {
        if (Objects.isNull(userSession)) {
            addMissingFieldError(USER_SESSION, errorList);
        } else {
            validateFieldNotEmpty(userSession.getSessionId(), SESSION_ID, errorList);
            validateFieldNotEmpty(userSession.getUserName(), USERNAME, errorList);
        }
    }

    // Appeal Validation

    void validateIoJAppeal(IojAppeal appeal, List<ErrorMessage> errorList) {
        if (Objects.isNull(appeal)) {
            addMissingFieldError(APPEAL, errorList);
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
    private void validateAppealReasonType(NewWorkReason appealReason, List<ErrorMessage> errorList) {
        if (Objects.nonNull(appealReason) && !appealReason.getType().equalsIgnoreCase(APPEAL_REASON_HARDIOJ)) {
            addErrorMessage(APPEAL_REASON, ERROR_APPEAL_REASON_IS_INVALID, errorList);
        }
    }

    // check appeal reason/appeal accessor combination is valid.
    private void validateAppealReasonAppealAssessorCombinations(IojAppeal appeal, List<ErrorMessage> errorList) {
        NewWorkReason reason = appeal.getAppealReason();
        IojAppealAssessor assessor = appeal.getAppealAssessor();
        // Null check Assessor as != includes nulls.
        if ((Objects.nonNull(assessor))
                && (NewWorkReason.NEW.equals(reason) && (!IojAppealAssessor.CASEWORKER.equals(assessor))
                        || (NewWorkReason.JR.equals(reason) && !IojAppealAssessor.JUDGE.equals(assessor)))) {
            addErrorMessage(APPEAL_ASSESSOR, ERROR_INCORRECT_COMBINATION, errorList);
        }
    }

    // Utility Methods
    private void validateFieldNotEmpty(
            Object field, ApiCreateIojAppealRequestFields fieldName, List<ErrorMessage> errorList) {
        if (ObjectUtils.isEmpty(field)) {
            addMissingFieldError(fieldName, errorList);
        }
    }

    private void addErrorMessage(
            ApiCreateIojAppealRequestFields fieldname, String errorMessage, List<ErrorMessage> errorList) {
        errorList.add(new ErrorMessage(fieldname.getName(), errorMessage));
    }

    private void addMissingFieldError(ApiCreateIojAppealRequestFields fieldName, List<ErrorMessage> errorList) {
        String errorMessage = String.format(ERROR_FIELD_IS_MISSING, fieldName.getName());
        addErrorMessage(fieldName, errorMessage, errorList);
    }
}
