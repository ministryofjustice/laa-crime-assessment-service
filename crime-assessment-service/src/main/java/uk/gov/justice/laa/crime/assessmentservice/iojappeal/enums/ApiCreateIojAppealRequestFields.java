package uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiCreateIojAppealRequestFields {
    REQUEST("Request"),

    METADATA("Metadata"),
    CASE_MANAGEMENT_UNIT("Case Management Unit"),
    LEGACY_APPLICATION_ID("Legacy Application Id"),
    USER_SESSION("User Session"),
    SESSION_ID("Session ID"),
    USERNAME("Username"),

    APPEAL("Appeal"),
    APPEAL_SUCCESSFUL("Appeal Successful"),
    APPEAL_ASSESSOR("Appeal Assessor"),
    APPEAL_REASON("Appeal Reason"),
    DECISION_REASON("Decision Reason"),
    DECISION_DATE("Decision Date"),
    RECEIVED_DATE("Received Date"),
    APPLICATION_RECEIVED_DATE("Application Received Date");

    private final String name;
}
