package uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiCreateIojAppealRequestFields {
    LEGACY_APPLICATION_ID("Legacy Application Id"),
    APPEAL_SUCCESSFUL("Appeal Successful"),
    APPEAL_ASSESSOR("Appeal Assessor"),
    APPEAL_REASON("Appeal Reason"),
    APPLICATION_RECEIVED_DATE("Application Received Date");

    private final String name;
}
