package uk.gov.justice.laa.crime.assessmentservice.audit.api;

public enum AuditPath {
    LOCAL_HIT,
    LOCAL_MISS,
    LOCAL_MISS_LEGACY_HIT,
    LOCAL_MISS_LEGACY_MISS,
    LOCAL_MISS_LEGACY_FAILURE,
    DUAL_WRITE_SUCCESS,
    DUAL_WRITE_FAILURE
}
