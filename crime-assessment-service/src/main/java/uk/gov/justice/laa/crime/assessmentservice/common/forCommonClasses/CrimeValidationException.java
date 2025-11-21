package uk.gov.justice.laa.crime.assessmentservice.common.forCommonClasses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// TODO : This needs to be moved into crime commons.

@Getter
@Setter
public class CrimeValidationException extends RuntimeException {
    private final List<String> exceptionMessage;

    public CrimeValidationException(List<String> exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
