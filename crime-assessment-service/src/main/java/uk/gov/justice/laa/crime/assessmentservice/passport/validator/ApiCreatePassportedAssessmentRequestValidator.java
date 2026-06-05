package uk.gov.justice.laa.crime.assessmentservice.passport.validator;

import static uk.gov.justice.laa.crime.assessmentservice.passport.enums.ApiCreatePassportedAssessmentRequestFields.LAST_SIGN_ON_DATE;

import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.crime.common.model.passported.ApiCreatePassportedAssessmentRequest;
import uk.gov.justice.laa.crime.common.model.passported.DeclaredBenefit;
import uk.gov.justice.laa.crime.enums.BenefitType;
import uk.gov.justice.laa.crime.error.ErrorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class ApiCreatePassportedAssessmentRequestValidator {
    public static final String ERROR_LAST_SIGN_ON_DATE_EMPTY =
            "Last sign on date required for job seekers benefit type.";

    public List<ErrorMessage> validateRequest(ApiCreatePassportedAssessmentRequest request) {
        var errorList = new ArrayList<ErrorMessage>();
        validateLastSignOnDate(request.getPassportedAssessment().getDeclaredBenefit())
                .ifPresent(errorList::add);
        return errorList;
    }

    private Optional<ErrorMessage> validateLastSignOnDate(DeclaredBenefit declaredBenefit) {
        if (declaredBenefit.getBenefitType().getCode().equals(BenefitType.JSA.getCode())
                && declaredBenefit.getLastSignOnDate() == null) {
            return Optional.of(new ErrorMessage(LAST_SIGN_ON_DATE.getName(), ERROR_LAST_SIGN_ON_DATE_EMPTY));
        }

        return Optional.empty();
    }
}
