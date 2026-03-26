package uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator;

import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.APPEAL_ASSESSOR;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.APPEAL_REASON;

import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.NewWorkReason;
import uk.gov.justice.laa.crime.error.ErrorMessage;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ApiCreateIojAppealRequestValidator {
    // error messages
    public static final String APPEAL_REASON_HARDIOJ = "HARDIOJ";
    public static final String ERROR_INCORRECT_COMBINATION = "Incorrect Combination of Assessor and Reason.";
    public static final String ERROR_APPEAL_REASON_IS_INVALID = "Appeal Reason is invalid.";

    public List<ErrorMessage> validateRequest(ApiCreateIojAppealRequest request) {
        var errorList = new ArrayList<ErrorMessage>();
        validateIoJAppeal(request.getIojAppeal(), errorList);
        return errorList;
    }

    void validateIoJAppeal(IojAppeal appeal, List<ErrorMessage> errorList) {
        validateAppealReasonType(appeal.getAppealReason(), errorList);
        validateAppealReasonAppealAssessorCombinations(appeal, errorList);
    }

    // check the appeal reason is of type HARDIOJ
    private void validateAppealReasonType(NewWorkReason appealReason, List<ErrorMessage> errorList) {
        if (appealReason != null && !APPEAL_REASON_HARDIOJ.equalsIgnoreCase(appealReason.getType())) {
            addErrorMessage(APPEAL_REASON, ERROR_APPEAL_REASON_IS_INVALID, errorList);
        }
    }

    private void validateAppealReasonAppealAssessorCombinations(IojAppeal appeal, List<ErrorMessage> errorList) {
        NewWorkReason reason = appeal.getAppealReason();
        IojAppealAssessor assessor = appeal.getAppealAssessor();
        // Null check Assessor as != includes nulls.
        if ((NewWorkReason.NEW.equals(reason) && !IojAppealAssessor.CASEWORKER.equals(assessor))
                || (NewWorkReason.JR.equals(reason) && !IojAppealAssessor.JUDGE.equals(assessor))) {
            addErrorMessage(APPEAL_ASSESSOR, ERROR_INCORRECT_COMBINATION, errorList);
        }
    }

    private void addErrorMessage(
            ApiCreateIojAppealRequestFields fieldName, String errorMessage, List<ErrorMessage> errorList) {
        errorList.add(new ErrorMessage(fieldName.getName(), errorMessage));
    }
}
