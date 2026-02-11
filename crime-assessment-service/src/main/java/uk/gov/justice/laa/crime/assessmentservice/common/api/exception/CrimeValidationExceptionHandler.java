package uk.gov.justice.laa.crime.assessmentservice.common.api.exception;

import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.common.api.validation.ValidationErrorDTO;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CrimeValidationExceptionHandler {

    private static ResponseEntity<ValidationErrorDTO> buildErrorResponse(List<String> errorMessage) {
        return new ResponseEntity<>(
                ValidationErrorDTO.builder()
                        .code(HttpStatus.BAD_REQUEST.toString())
                        .messageList(errorMessage)
                        .build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CrimeValidationException.class)
    public ResponseEntity<ValidationErrorDTO> handleCrimeValidationException(CrimeValidationException ex) {
        return buildErrorResponse(new ArrayList<>(ex.getExceptionMessage()));
    }
}
