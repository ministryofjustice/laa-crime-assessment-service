package uk.gov.justice.laa.crime.assessmentservice.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.common.api.advice.ApiError;
import uk.gov.justice.laa.crime.assessmentservice.common.api.exception.AssessmentCreateException;
import uk.gov.justice.laa.crime.assessmentservice.common.api.exception.CrimeValidationException;
import uk.gov.justice.laa.crime.assessmentservice.common.api.exception.DefaultExceptionHandler;
import uk.gov.justice.laa.crime.assessmentservice.common.api.exception.RequestedObjectNotFoundException;
import uk.gov.justice.laa.crime.error.ErrorExtension;
import uk.gov.justice.laa.crime.error.ErrorMessage;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;
import uk.gov.justice.laa.crime.util.ProblemDetailUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class DefaultExceptionHandlerTest {

    private static final String TRACE_ID = "test-trace-id";

    @Mock
    private ObjectProvider<TraceIdHandler> traceIdHandlerProvider;

    @Mock
    private TraceIdHandler traceIdHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DefaultExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DefaultExceptionHandler(traceIdHandlerProvider);
        when(traceIdHandlerProvider.getIfAvailable()).thenReturn(traceIdHandler);
        lenient().when(traceIdHandler.getTraceId()).thenReturn(TRACE_ID);
    }

    @Test
    void givenParseableProblemDetail_whenHandleWebClientResponseException_thenReturnsParsedResponse() throws Exception {
        List<ErrorMessage> expectedErrors = List.of(new ErrorMessage("field", "downstream validation failed"));

        ErrorExtension extension =
                ProblemDetailUtil.buildErrorExtension(ApiError.APPLICATION_ERROR.code(), TRACE_ID, expectedErrors);

        ProblemDetail downstreamProblemDetail =
                ProblemDetailUtil.buildProblemDetail(HttpStatus.BAD_REQUEST, "Downstream detail", extension);

        String responseBody = objectMapper.writeValueAsString(downstreamProblemDetail);

        WebClientResponseException exception = WebClientResponseException.create(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                HttpHeaders.EMPTY,
                responseBody.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        ResponseEntity<ProblemDetail> response = handler.handleWebClientResponseException(exception);

        assertProblemDetail(
                response,
                HttpStatus.BAD_REQUEST,
                downstreamProblemDetail.getDetail(),
                ApiError.APPLICATION_ERROR.code(),
                expectedErrors);
    }

    @Test
    void givenUnparseableProblemDetail_whenHandleWebClientResponseException_thenReturnsOriginalStatusAndMessage() {
        WebClientResponseException exception = WebClientResponseException.create(
                HttpStatus.BAD_GATEWAY.value(),
                "Bad Gateway",
                HttpHeaders.EMPTY,
                "not-json".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        ResponseEntity<ProblemDetail> response = handler.handleWebClientResponseException(exception);

        assertProblemDetail(
                response, HttpStatus.BAD_GATEWAY, exception.getMessage(), ApiError.APPLICATION_ERROR.code(), List.of());
    }

    @Test
    void givenRequestedObjectNotFoundException_whenHandleNotFound_thenReturns404() {
        RequestedObjectNotFoundException exception =
                new RequestedObjectNotFoundException("IOJ appeal not found for appealId: 123");

        ResponseEntity<ProblemDetail> response = handler.handleNotFound(exception);

        assertProblemDetail(
                response, HttpStatus.NOT_FOUND, exception.getMessage(), ApiError.OBJECT_NOT_FOUND.code(), List.of());
    }

    @Test
    void givenWebClientRequestException_whenHandleWebClientRequestException_thenReturns500() {
        WebClientRequestException exception = mock(WebClientRequestException.class);

        ResponseEntity<ProblemDetail> response = handler.handleWebClientRequestException(exception);

        assertProblemDetail(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiError.APPLICATION_ERROR.defaultDetail(),
                ApiError.APPLICATION_ERROR.code(),
                List.of());
    }

    @Test
    void givenMethodArgumentNotValidException_whenHandleSchemaValidationFailure_thenReturns400WithFieldErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = List.of(
                new FieldError("request", "iojAppealMetadata", "must not be null"),
                new FieldError("request", "iojAppeal", "must not be null"));

        List<ErrorMessage> expectedErrors = List.of(
                new ErrorMessage("iojAppealMetadata", "must not be null"),
                new ErrorMessage("iojAppeal", "must not be null"));

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(exception.getMessage()).thenReturn("Bean validation failed");

        ResponseEntity<ProblemDetail> response = handler.handleSchemaValidationFailure(exception);

        assertProblemDetail(
                response,
                HttpStatus.BAD_REQUEST,
                ApiError.VALIDATION_FAILURE.defaultDetail(),
                ApiError.VALIDATION_FAILURE.code(),
                expectedErrors);
    }

    @Test
    void givenCrimeValidationException_whenHandleValidationFailure_thenReturns400WithErrors() {
        List<ErrorMessage> expectedErrors = List.of(
                new ErrorMessage("appealReason", "Appeal Reason is invalid."),
                new ErrorMessage("appealAssessor", "Incorrect Combination of Assessor and Reason."));

        CrimeValidationException exception = new CrimeValidationException(expectedErrors);

        ResponseEntity<ProblemDetail> response = handler.handleValidationFailure(exception);

        assertProblemDetail(
                response,
                HttpStatus.BAD_REQUEST,
                ApiError.VALIDATION_FAILURE.defaultDetail(),
                ApiError.VALIDATION_FAILURE.code(),
                expectedErrors);
    }

    @Test
    void givenDataIntegrityViolationException_whenHandleDataIntegrityViolation_thenReturns400() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate key");

        ResponseEntity<ProblemDetail> response = handler.handleDataIntegrityViolation(exception);

        assertProblemDetail(
                response,
                HttpStatus.BAD_REQUEST,
                ApiError.DB_ERROR.defaultDetail(),
                ApiError.DB_ERROR.code(),
                List.of());
    }

    @Test
    void givenAssessmentRollbackException_whenHandleRollbackFailure_thenReturns500WithExceptionMessage() {
        AssessmentCreateException exception = new AssessmentCreateException("Rollback failed");

        ResponseEntity<ProblemDetail> response = handler.handleRollbackFailure(exception);

        assertProblemDetail(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Rollback failed",
                ApiError.APPLICATION_ERROR.code(),
                List.of());
    }

    @Test
    void givenUnhandledException_whenHandleUnhandled_thenReturns500() {
        Exception exception = new RuntimeException("Unexpected");

        ResponseEntity<ProblemDetail> response = handler.handleUnhandled(exception);

        assertProblemDetail(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiError.APPLICATION_ERROR.defaultDetail(),
                ApiError.APPLICATION_ERROR.code(),
                List.of());
    }

    @Test
    void givenNoTraceIdHandler_whenHandleUnhandled_thenReturnsEmptyTraceId() {
        when(traceIdHandlerProvider.getIfAvailable()).thenReturn(null);

        ResponseEntity<ProblemDetail> response = handler.handleUnhandled(new RuntimeException("Unexpected"));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        ErrorExtension errorsExtension = getErrorExtension(response.getBody());
        assertThat(errorsExtension.traceId()).isEmpty();
    }

    @Test
    void givenMethodArgumentTypeMismatchException_whenHandleBadRequest_thenReturns400() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getMessage()).thenReturn("Failed to convert value");

        ResponseEntity<ProblemDetail> response = handler.handleBadRequest(exception);

        assertProblemDetail(
                response,
                HttpStatus.BAD_REQUEST,
                ApiError.BAD_REQUEST.defaultDetail(),
                ApiError.BAD_REQUEST.code(),
                List.of());
    }

    @Test
    void givenHttpMessageNotReadableException_whenHandleBadRequest_thenReturns400() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn("Malformed JSON request");

        ResponseEntity<ProblemDetail> response = handler.handleBadRequest(exception);

        assertProblemDetail(
                response,
                HttpStatus.BAD_REQUEST,
                ApiError.BAD_REQUEST.defaultDetail(),
                ApiError.BAD_REQUEST.code(),
                List.of());
    }

    @Test
    void givenHttpRequestMethodNotSupportedException_whenHandleMethodNotSupported_thenReturns405() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("PUT");

        ResponseEntity<ProblemDetail> response = handler.handleMethodNotSupported(exception);

        assertProblemDetail(
                response,
                HttpStatus.METHOD_NOT_ALLOWED,
                ApiError.METHOD_NOT_ALLOWED.defaultDetail(),
                ApiError.METHOD_NOT_ALLOWED.code(),
                List.of());
    }

    @Test
    void givenHttpMediaTypeNotSupportedException_whenHandleUnsupportedMediaType_thenReturns415() {
        HttpMediaTypeNotSupportedException exception = new HttpMediaTypeNotSupportedException("application/xml");

        ResponseEntity<ProblemDetail> response = handler.handleUnsupportedMediaType(exception);

        assertProblemDetail(
                response,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ApiError.UNSUPPORTED_MEDIA_TYPE.defaultDetail(),
                ApiError.UNSUPPORTED_MEDIA_TYPE.code(),
                List.of());
    }

    private void assertProblemDetail(
            ResponseEntity<ProblemDetail> response,
            HttpStatus expectedStatus,
            String expectedDetail,
            String expectedCode,
            List<ErrorMessage> expectedErrors) {

        ProblemDetail body = response.getBody();

        assertThat(body).isNotNull();
        assertThat(body.getDetail()).isEqualTo(expectedDetail);
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);

        ErrorExtension errorExtension = getErrorExtension(body);

        assertThat(errorExtension.traceId()).isEqualTo(TRACE_ID);
        assertThat(errorExtension.code()).isEqualTo(expectedCode);
        assertThat(errorExtension.errors()).containsExactlyInAnyOrderElementsOf(expectedErrors);
    }

    private ErrorExtension getErrorExtension(ProblemDetail problemDetail) {
        Optional<ErrorExtension> errorExtension = ProblemDetailUtil.getErrorExtension(problemDetail);
        assertThat(errorExtension).isPresent();
        return errorExtension.get();
    }
}
