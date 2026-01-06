package uk.gov.justice.laa.crime.assessmentservice.common.exception;

public class AssessmentServiceException extends RuntimeException {

    public AssessmentServiceException(String exceptionMessage) {
        super(exceptionMessage);
    }
}
