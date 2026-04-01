package uk.gov.justice.laa.crime.assessmentservice.common.api.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.error.ErrorMessage;
import uk.gov.justice.laa.crime.error.ProblemDetailError;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;
import uk.gov.justice.laa.crime.util.ProblemDetailUtil;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class DefaultExceptionHandler {
    private final ObjectProvider<TraceIdHandler> traceIdHandlerProvider;

    private String getTraceId() {
        return Optional.ofNullable(traceIdHandlerProvider.getIfAvailable())
                .map(TraceIdHandler::getTraceId)
                .orElse("");
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ProblemDetail> handleWebClientResponseException(WebClientResponseException ex) {
        try {
            ProblemDetail problemDetail = ProblemDetailUtil.parseProblemDetailJson(ex.getResponseBodyAsString());

            return buildResponse(
                    ex.getStatusCode(),
                    ProblemDetailError.APPLICATION_ERROR,
                    problemDetail.getDetail(),
                    ProblemDetailUtil.getErrorMessages(problemDetail));

        } catch (JsonProcessingException parseEx) {
            log.warn("Unable to read error response. TraceId={}", getTraceId(), parseEx);
            return buildResponse(ex.getStatusCode(), ProblemDetailError.APPLICATION_ERROR, ex.getMessage(), List.of());
        }
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ProblemDetail> handleBadRequest(Exception ex) {
        log.warn("Bad request. TraceId={} Detail={}", getTraceId(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ProblemDetailError.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported. TraceId={} Detail={}", getTraceId(), ex.getMessage());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ProblemDetailError.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        log.warn("Unsupported media type. TraceId={} Detail={}", getTraceId(), ex.getMessage());
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ProblemDetailError.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(RequestedObjectNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(RequestedObjectNotFoundException ex) {
        log.info("Resource not found. TraceId={} Detail={}", getTraceId(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ProblemDetailError.OBJECT_NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ProblemDetail> handleWebClientRequestException(WebClientRequestException ex) {
        log.error("Request to service failed. TraceId={}", getTraceId(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ProblemDetailError.APPLICATION_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleSchemaValidationFailure(MethodArgumentNotValidException ex) {

        log.warn("Bean validation failed. TraceId={} Detail={}", getTraceId(), ex.getMessage());
        var messages = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErrorMessage(e.getField(), e.getDefaultMessage()))
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, ProblemDetailError.VALIDATION_FAILURE, messages);
    }

    @ExceptionHandler(CrimeValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidationFailure(CrimeValidationException ex) {
        log.warn(
                "Crime validation exception. TraceId={} Errors={} Detail={}",
                getTraceId(),
                ex.getExceptionMessages().size(),
                String.join(
                        ", ",
                        ex.getExceptionMessages().stream()
                                .map(ErrorMessage::message)
                                .toList()));
        return buildResponse(HttpStatus.BAD_REQUEST, ProblemDetailError.VALIDATION_FAILURE, ex.getExceptionMessages());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("DB constraint violation. TraceId={}", getTraceId(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ProblemDetailError.DB_ERROR);
    }

    @ExceptionHandler(AssessmentRollbackException.class)
    public ResponseEntity<ProblemDetail> handleRollbackFailure(AssessmentRollbackException ex) {
        log.error("Assessment rollback failed. TraceId={}", getTraceId(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, ProblemDetailError.APPLICATION_ERROR, ex.getMessage(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnhandled(Exception ex) {
        log.error("Unhandled exception. TraceId={}", getTraceId(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ProblemDetailError.APPLICATION_ERROR);
    }

    private ResponseEntity<ProblemDetail> buildResponse(
            HttpStatusCode status, ProblemDetailError error, String detailOverride, List<ErrorMessage> errors) {
        return ResponseEntity.status(status)
                .body(ProblemDetailUtil.buildProblemDetail(
                        status,
                        detailOverride,
                        ProblemDetailUtil.buildErrorExtension(error.code(), getTraceId(), errors)));
    }

    private ResponseEntity<ProblemDetail> buildResponse(HttpStatusCode status, ProblemDetailError error) {
        return ResponseEntity.status(status)
                .body(ProblemDetailUtil.buildProblemDetail(status, error, getTraceId(), List.of()));
    }

    private ResponseEntity<ProblemDetail> buildResponse(
            HttpStatusCode status, ProblemDetailError error, List<ErrorMessage> errors) {

        return ResponseEntity.status(status)
                .body(ProblemDetailUtil.buildProblemDetail(status, error, getTraceId(), errors));
    }
}
