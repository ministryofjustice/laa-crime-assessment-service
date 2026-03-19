package uk.gov.justice.laa.crime.assessmentservice.common.api.exception;

public class RequestedObjectNotFoundException extends RuntimeException {

    public RequestedObjectNotFoundException(String message) {
        super(message);
    }
}
