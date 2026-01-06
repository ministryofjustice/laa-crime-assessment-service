package uk.gov.justice.laa.crime.assessmentservice.common.exception;

public class AssessmentServiceException extends RuntimeException {

    private final String exceptionMessage;

    public AssessmentServiceException(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
