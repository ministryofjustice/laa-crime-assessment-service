package uk.gov.justice.laa.crime.assessmentservice.common.api.exception;

public class AssessmentCreateException extends RuntimeException {

    public AssessmentCreateException(String exceptionMessage) {
        super(exceptionMessage);
    }
}
