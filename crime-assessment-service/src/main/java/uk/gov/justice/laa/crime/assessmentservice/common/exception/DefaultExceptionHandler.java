package uk.gov.justice.laa.crime.assessmentservice.common.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.dto.ErrorDTO;
import uk.gov.justice.laa.crime.error.ErrorExtension;
import uk.gov.justice.laa.crime.error.ErrorMessage;
import uk.gov.justice.laa.crime.exception.ValidationException;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;
import uk.gov.justice.laa.crime.util.ProblemDetailUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
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
    private final TraceIdHandler traceIdHandler;

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ProblemDetail> onRuntimeException(WebClientResponseException exception) {
        String errorMessage;
        try {
            ErrorDTO errorDTO = mapper.readValue(exception.getResponseBodyAsString(), ErrorDTO.class);
            errorMessage = errorDTO.getMessage();
        } catch (IOException ex) {
            log.warn("Unable to read the ErrorDTO from WebClientResponseException", ex);
            errorMessage = exception.getMessage();
        }
        return buildSimpleErrorResponse(
                exception.getStatusCode(), exception.getStatusCode().toString(), errorMessage);
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ProblemDetail> onRuntimeException(WebClientRequestException exception) {
        return buildSimpleErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", exception.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(ValidationException exception) {
        return buildSimpleErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failure", exception.getMessage());
    }

    @ExceptionHandler(CrimeValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(CrimeValidationException exception) {
        ErrorExtension extension = buildErrorExtension(
                "VALIDATION_FAILURE", traceIdHandler.getTraceId(), exception.getExceptionMessages());
        return buildSimpleErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failure", extension);
    }

    @ExceptionHandler(AssessmentRollbackException.class)
    public ResponseEntity<ProblemDetail> handleAssessmentRollbackException(AssessmentRollbackException exception) {
        ErrorExtension extension =
                buildErrorExtension("ROLLBACK", traceIdHandler.getTraceId(), Collections.emptyList());
        return buildSimpleErrorResponse(HttpStatusCode.valueOf(555), exception.getMessage(), extension);
    }

    private ResponseEntity<ProblemDetail> buildSimpleErrorResponse(
            HttpStatusCode status, String message, ErrorExtension extension) {
        logError(status.toString(), message);
        return new ResponseEntity<>(ProblemDetailUtil.buildProblemDetail(status, message, extension), status);
    }

    private ResponseEntity<ProblemDetail> buildSimpleErrorResponse(
            HttpStatusCode status, String errorCode, String message) {
        logError(status.toString(), message);
        // create extension with empty list. Detail will suffice.
        ErrorExtension extension = buildErrorExtension(errorCode, traceIdHandler.getTraceId(), List.of());
        return buildSimpleErrorResponse(status, message, extension);
    }

    private ErrorExtension buildErrorExtension(String code, String traceId, List<ErrorMessage> errorMessages) {
        return ProblemDetailUtil.buildErrorExtension(code, traceId, errorMessages);
    }

    private void logError(String status, String message) {
        log.error(
                "Exception Occurred. Status - {}, Detail - {}, TraceId - {}",
                status,
                message,
                traceIdHandler.getTraceId());
    }
}
