package uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator;

import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.APPEAL_ASSESSOR;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields.APPEAL_REASON;

import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.NewWorkReason;
import uk.gov.justice.laa.crime.error.ErrorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class ApiCreateIojAppealRequestValidator {
    // error messages
    public static final String APPEAL_REASON_HARDIOJ = "HARDIOJ";
    public static final String ERROR_INCORRECT_COMBINATION = "Incorrect Combination of Assessor and Reason.";
    public static final String ERROR_APPEAL_REASON_IS_INVALID = "Appeal Reason is invalid.";

    public List<ErrorMessage> validateRequest(ApiCreateIojAppealRequest request) {
        var errorList = new ArrayList<ErrorMessage>();
        validateAppealReasonType(request.getIojAppeal().getAppealReason()).ifPresent(errorList::add);
        validateAppealReasonAppealAssessorCombinations(request.getIojAppeal()).ifPresent(errorList::add);
        return errorList;
    }

    // check the appeal reason is of type HARDIOJ
    private Optional<ErrorMessage> validateAppealReasonType(NewWorkReason appealReason) {
        if (appealReason != null && !APPEAL_REASON_HARDIOJ.equalsIgnoreCase(appealReason.getType())) {
            return Optional.of(new ErrorMessage(APPEAL_REASON.getName(), ERROR_APPEAL_REASON_IS_INVALID));
        }

        return Optional.empty();
    }

    private Optional<ErrorMessage> validateAppealReasonAppealAssessorCombinations(IojAppeal appeal) {
        NewWorkReason reason = appeal.getAppealReason();
        IojAppealAssessor assessor = appeal.getAppealAssessor();
        // Null check Assessor as != includes nulls.
        if ((NewWorkReason.NEW.equals(reason) && !IojAppealAssessor.CASEWORKER.equals(assessor))
                || (NewWorkReason.JR.equals(reason) && !IojAppealAssessor.JUDGE.equals(assessor))) {
            return Optional.of(new ErrorMessage(APPEAL_ASSESSOR.getName(), ERROR_INCORRECT_COMBINATION));
        }

        return Optional.empty();
    }
}
