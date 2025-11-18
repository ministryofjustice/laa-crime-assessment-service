package uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator;

import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppealMetadata;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.NewWorkReason;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class ApiCreateIojAppealRequestValidator {
    private static final String FIELD_IS_MISSING = "%s is missing.";

    public List<String> validateRequest(ApiCreateIojAppealRequest request) {
        var errorList = new ArrayList<String>();

        // check the IojAppealMetadata Object.
        errorList.addAll(validateMetaData(request));
        // check the IojAppeal Object.
        errorList.addAll(validateIoJAppeal(request));

        return errorList;
    }

    // MetaData Validation

    List<String> validateMetaData(ApiCreateIojAppealRequest request) {
        List<String> errorList = new ArrayList<>();
        IojAppealMetadata metadata = request.getIojAppealMetadata();

        if (Objects.isNull(metadata)) {
            errorList.add(String.format(FIELD_IS_MISSING, "Metadata"));
            return errorList;
        }
        // nullchecks
        validateFieldNotNull(metadata.getCaseManagementUnitId(), "Case Management Unit", errorList);
        validateFieldNotNull(metadata.getUserSession(), "User Session", errorList);
        validateAnApplicationIdPresent(metadata, errorList);
        return errorList;
    }

    // Ensure that we have an application ID. Either LegacyApplicationId, or ApplicationId must be present.
    private void validateAnApplicationIdPresent(IojAppealMetadata metadata, List<String> errorList) {
        if (Objects.isNull(metadata.getApplicationId()) && Objects.isNull(metadata.getLegacyApplicationId())) {
            errorList.add(String.format(FIELD_IS_MISSING, "Both Application Id and Legacy Application Id"));
        }
    }

    // Appeal Validation

    List<String> validateIoJAppeal(ApiCreateIojAppealRequest request) {
        List<String> errorList = new ArrayList<>();

        IojAppeal appeal = request.getIojAppeal();
        if (Objects.isNull(appeal)) {
            errorList.add(String.format(FIELD_IS_MISSING, "Appeal"));
            return errorList;
        }
        // validate notNull
        validateFieldNotNull(appeal.getAppealDecision(), "Appeal Decision", errorList);
        validateFieldNotNull(appeal.getAppealAssessor(), "Appeal Assessor", errorList);
        validateFieldNotNull(appeal.getAppealReason(), "Appeal Reason", errorList);
        validateFieldNotNull(appeal.getDecisionReason(), "Decision Reason", errorList);
        validateFieldNotNull(appeal.getDecisionDate(), "Decision Date", errorList);
        validateFieldNotNull(appeal.getReceivedDate(), "Received Date", errorList);
        // validate content
        validateAppealReasonType(appeal.getAppealReason(), errorList);
        validateAppealReasonAppealAssessorCombinations(appeal, errorList);
        return errorList;
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
        // If reason = NEW, then assessor == CASEWORKER
        if ((NewWorkReason.getFrom("NEW").equals(reason) && !IojAppealAssessor.CASEWORKER.equals(assessor))
                || (NewWorkReason.getFrom("JR").equals(reason) && !IojAppealAssessor.JUDGE.equals(assessor))) {
            errorList.add("Incorrect Combination of Assessor and Reason.");
        }
    }

    // Utility Methods
    private void validateFieldNotNull(Object field, String fieldName, List<String> errorList) {
        if (Objects.isNull(field)) {
            errorList.add(String.format(FIELD_IS_MISSING, fieldName));
        }
    }
}
