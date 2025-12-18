package uk.gov.justice.laa.crime.assessmentservice.common.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import uk.gov.justice.laa.crime.dto.ErrorDTO;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class DefaultExceptionHandler {

    private final ObjectMapper mapper;

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorDTO> onRuntimeException(WebClientResponseException exception) {
        String errorMessage;
        try {
            ErrorDTO errorDTO = mapper.readValue(exception.getResponseBodyAsString(),
                ErrorDTO.class);
            errorMessage = errorDTO.getMessage();
        } catch (IOException ex) {
            log.warn("Unable to read the ErrorDTO from WebClientResponseException", ex);
            errorMessage = exception.getMessage();
        }
        return buildErrorResponse(exception.getStatusCode(), errorMessage);
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorDTO> onRuntimeException(WebClientRequestException exception) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDTO> handleValidationException(ValidationException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(AssessmentServiceException.class)
    public ResponseEntity<ErrorDTO> handleAssessmentServiceException(
        AssessmentServiceException exception) {
        return buildErrorResponse(HttpStatusCode.valueOf(555), exception.getMessage());
    }

    private static ResponseEntity<ErrorDTO> buildErrorResponse(HttpStatusCode status,
        String message) {
        log.error("Exception Occurred. Status - {}, Detail - {}, TraceId - {}", status, message);
        return new ResponseEntity<>(
            ErrorDTO.builder().code(status.toString()).message(message).build(), status);
    }
}
