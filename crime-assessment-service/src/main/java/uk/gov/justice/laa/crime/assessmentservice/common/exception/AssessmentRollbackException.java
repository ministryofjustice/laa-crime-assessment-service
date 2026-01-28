package uk.gov.justice.laa.crime.assessmentservice.common.exception;

public class AssessmentRollbackException extends RuntimeException {

    public AssessmentRollbackException(String exceptionMessage) {
        super(exceptionMessage);
    }
}
