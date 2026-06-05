package uk.gov.justice.laa.crime.assessmentservice.passport.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiCreatePassportedAssessmentRequestFields {
    LAST_SIGN_ON_DATE("Last Sign on Date");

    private final String name;
}
