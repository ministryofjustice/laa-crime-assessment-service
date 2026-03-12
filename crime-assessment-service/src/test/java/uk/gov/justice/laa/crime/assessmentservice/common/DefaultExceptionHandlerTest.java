package uk.gov.justice.laa.crime.assessmentservice.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.common.api.advice.ProblemDetailError;
import uk.gov.justice.laa.crime.assessmentservice.common.api.exception.AssessmentRollbackException;
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
        ErrorExtension extension = ProblemDetailUtil.buildErrorExtension(
                ProblemDetailError.APPLICATION_ERROR.code(),
                TRACE_ID,
                List.of(new ErrorMessage("field", "downstream validation failed")));

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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("Downstream detail");
        assertErrorExtension(response.getBody(), ProblemDetailError.APPLICATION_ERROR.code(), 1);
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

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo(exception.getMessage());
        assertErrorExtension(response.getBody(), ProblemDetailError.APPLICATION_ERROR.code(), 0);
    }

    @Test
    void givenRequestedObjectNotFoundException_whenHandleNotFound_thenReturns404() {
        RequestedObjectNotFoundException exception =
                new RequestedObjectNotFoundException("IOJ appeal not found for appealId: 123");

        ResponseEntity<ProblemDetail> response = handler.handleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo(exception.getMessage());
        assertErrorExtension(response.getBody(), ProblemDetailError.OBJECT_NOT_FOUND.code(), 0);
    }

    @Test
    void givenWebClientRequestException_whenHandleWebClientRequestException_thenReturns500() {
        WebClientRequestException exception = mock(WebClientRequestException.class);

        ResponseEntity<ProblemDetail> response = handler.handleWebClientRequestException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo(ProblemDetailError.APPLICATION_ERROR.defaultDetail());
        assertErrorExtension(response.getBody(), ProblemDetailError.APPLICATION_ERROR.code(), 0);
    }

    @Test
    void givenMethodArgumentNotValidException_whenHandleSchemaValidationFailure_thenReturns400WithFieldErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = List.of(
                new FieldError("request", "iojAppealMetadata", "must not be null"),
                new FieldError("request", "iojAppeal", "must not be null"));

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(exception.getMessage()).thenReturn("Bean validation failed");

        ResponseEntity<ProblemDetail> response = handler.handleSchemaValidationFailure(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo(ProblemDetailError.VALIDATION_FAILURE.defaultDetail());

        ErrorExtension errorsExtension = getErrorsExtension(response.getBody());
        assertThat(errorsExtension.code()).isEqualTo(ProblemDetailError.VALIDATION_FAILURE.code());
        assertThat(errorsExtension.traceId()).isEqualTo(TRACE_ID);

        assertThat(errorsExtension.errors()).hasSize(2);

        ErrorMessage firstError = errorsExtension.errors().getFirst();
        assertThat(firstError.field()).isEqualTo("iojAppealMetadata");
        assertThat(firstError.message()).isEqualTo("must not be null");
    }

    @Test
    void givenCrimeValidationException_whenHandleValidationFailure_thenReturns400WithErrors() {
        CrimeValidationException exception = new CrimeValidationException(List.of(
                new ErrorMessage("appealReason", "Appeal Reason is invalid."),
                new ErrorMessage("appealAssessor", "Incorrect Combination of Assessor and Reason.")));

        ResponseEntity<ProblemDetail> response = handler.handleValidationFailure(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo(ProblemDetailError.VALIDATION_FAILURE.defaultDetail());
        assertErrorExtension(response.getBody(), ProblemDetailError.VALIDATION_FAILURE.code(), 2);
    }

    @Test
    void givenDataIntegrityViolationException_whenHandleDataIntegrityViolation_thenReturns400() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate key");

        ResponseEntity<ProblemDetail> response = handler.handleDataIntegrityViolation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo(ProblemDetailError.DB_ERROR.defaultDetail());
        assertErrorExtension(response.getBody(), ProblemDetailError.DB_ERROR.code(), 0);
    }

    @Test
    void givenAssessmentRollbackException_whenHandleRollbackFailure_thenReturns500WithExceptionMessage() {
        AssessmentRollbackException exception = new AssessmentRollbackException("Rollback failed");

        ResponseEntity<ProblemDetail> response = handler.handleRollbackFailure(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("Rollback failed");
        assertErrorExtension(response.getBody(), ProblemDetailError.APPLICATION_ERROR.code(), 0);
    }

    @Test
    void givenUnhandledException_whenHandleUnhandled_thenReturns500() {
        Exception exception = new RuntimeException("Unexpected");

        ResponseEntity<ProblemDetail> response = handler.handleUnhandled(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo(ProblemDetailError.APPLICATION_ERROR.defaultDetail());
        assertErrorExtension(response.getBody(), ProblemDetailError.APPLICATION_ERROR.code(), 0);
    }

    @Test
    void givenNoTraceIdHandler_whenHandleUnhandled_thenReturnsEmptyTraceId() {
        when(traceIdHandlerProvider.getIfAvailable()).thenReturn(null);

        ResponseEntity<ProblemDetail> response = handler.handleUnhandled(new RuntimeException("Unexpected"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        ErrorExtension errorsExtension = getErrorsExtension(response.getBody());
        assertThat(errorsExtension.traceId()).isEmpty();
    }

    @Test
    void givenMethodArgumentTypeMismatchException_whenHandleBadRequest_thenReturns400() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getMessage()).thenReturn("Failed to convert value");

        ResponseEntity<ProblemDetail> response = handler.handleBadRequest(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo(ProblemDetailError.BAD_REQUEST.defaultDetail());
        assertErrorExtension(response.getBody(), ProblemDetailError.BAD_REQUEST.code(), 0);
    }

    @Test
    void givenHttpMessageNotReadableException_whenHandleBadRequest_thenReturns400() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn("Malformed JSON request");

        ResponseEntity<ProblemDetail> response = handler.handleBadRequest(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo(ProblemDetailError.BAD_REQUEST.defaultDetail());
        assertErrorExtension(response.getBody(), ProblemDetailError.BAD_REQUEST.code(), 0);
    }

    @Test
    void givenHttpRequestMethodNotSupportedException_whenHandleMethodNotSupported_thenReturns405() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("PUT");

        ResponseEntity<ProblemDetail> response = handler.handleMethodNotSupported(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo(ProblemDetailError.METHOD_NOT_ALLOWED.defaultDetail());
        assertErrorExtension(response.getBody(), ProblemDetailError.METHOD_NOT_ALLOWED.code(), 0);
    }

    @Test
    void givenHttpMediaTypeNotSupportedException_whenHandleUnsupportedMediaType_thenReturns415() {
        HttpMediaTypeNotSupportedException exception = new HttpMediaTypeNotSupportedException("application/xml");

        ResponseEntity<ProblemDetail> response = handler.handleUnsupportedMediaType(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo(ProblemDetailError.UNSUPPORTED_MEDIA_TYPE.defaultDetail());
        assertErrorExtension(response.getBody(), ProblemDetailError.UNSUPPORTED_MEDIA_TYPE.code(), 0);
    }

    private void assertErrorExtension(ProblemDetail problemDetail, String expectedCode, int expectedErrorCount) {

        ErrorExtension errorsExtension = getErrorsExtension(problemDetail);

        assertThat(errorsExtension.code()).isEqualTo(expectedCode);
        assertThat(errorsExtension.traceId()).isEqualTo(TRACE_ID);
        assertThat(errorsExtension.errors()).hasSize(expectedErrorCount);
    }

    private ErrorExtension getErrorsExtension(ProblemDetail problemDetail) {
        Optional<ErrorExtension> errorExtension = ProblemDetailUtil.getErrorExtension(problemDetail);
        assertThat(errorExtension).isPresent();
        return errorExtension.get();
    }
}
