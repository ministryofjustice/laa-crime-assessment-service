package uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiCreateIojAppealRequestFields {
    APPEAL_ASSESSOR("Appeal Assessor"),
    APPEAL_REASON("Appeal Reason");

    private final String name;
}
