package uk.gov.justice.laa.crime.assessmentservice.audit.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.audit.internal.helper.ClientIdResolver;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IojAuditRecorderTest {

    public static final String TRACE_ID = "trace-abc";
    public static final String CLIENT_ID = "client-123";

    @Mock
    private AuditEventRecorder audit;

    @Mock
    private TraceIdHandler traceIdHandler;

    @Mock
    private ClientIdResolver clientIdResolver;

    @InjectMocks
    private IojAuditRecorder iojAuditRecorder;

    @Captor
    private ArgumentCaptor<AuditEventRequest> requestCaptor;

    @Test
    void givenFoundTrue_whenRecordFindByAppealId_thenSuccessLocalHitPayloadAndAppealIdIdentifierAreRecorded() {
        UUID appealId = UUID.randomUUID();
        when(clientIdResolver.resolveOrAnonymous()).thenReturn(CLIENT_ID);
        when(traceIdHandler.getTraceId()).thenReturn(TRACE_ID);

        iojAuditRecorder.recordFindByAppealId(appealId, true);

        AuditEventRequest req = captureSingleRecordedRequest();

        assertThat(req.eventType()).isEqualTo(AuditEventType.FIND);
        assertThat(req.triggeredBy()).isEqualTo(CLIENT_ID);
        assertThat(req.traceId()).isEqualTo(TRACE_ID);

        assertThat(req.getIdentifierByName(AuditIdentifierType.APPEAL_ID))
                .isPresent()
                .get()
                .extracting(AuditIdentifier::value)
                .isEqualTo(appealId.toString());

        Map<String, Object> payload = payload(req);
        assertThat(payload).containsEntry("path", AuditPath.LOCAL_HIT).containsEntry("outcome", AuditOutcome.SUCCESS);

        assertThat(details(payload)).isInstanceOf(Map.class);
    }

    @Test
    void givenFoundFalse_whenRecordFindByAppealId_thenNotFoundLocalMissPayloadAndNoAppealIdIdentifierAreRecorded() {
        UUID appealId = UUID.randomUUID();
        when(clientIdResolver.resolveOrAnonymous()).thenReturn(CLIENT_ID);
        when(traceIdHandler.getTraceId()).thenReturn(TRACE_ID);

        iojAuditRecorder.recordFindByAppealId(appealId, false);

        AuditEventRequest req = captureSingleRecordedRequest();

        assertThat(req.eventType()).isEqualTo(AuditEventType.FIND);
        assertThat(req.triggeredBy()).isEqualTo(CLIENT_ID);
        assertThat(req.traceId()).isEqualTo(TRACE_ID);

        assertThat(req.getIdentifierByName(AuditIdentifierType.APPEAL_ID)).isEmpty();

        Map<String, Object> payload = payload(req);
        assertThat(payload)
                .containsEntry("path", AuditPath.LOCAL_MISS)
                .containsEntry("outcome", AuditOutcome.NOT_FOUND);

        Map<String, Object> details = details(payload);
        assertThat(details.toString()).contains(appealId.toString());
    }

    @ParameterizedTest(name = "givenLegacyId_whenRecordFindByLegacy_found={0}_thenPath={1}_andOutcome={2}")
    @MethodSource("legacyFindCases")
    void givenLegacyId_whenRecordFindByLegacy_thenPayloadIsRecorded(
            boolean found, AuditPath expectedPath, AuditOutcome expectedOutcome) {

        int legacyAppealId = 123;
        when(clientIdResolver.resolveOrAnonymous()).thenReturn(CLIENT_ID);
        when(traceIdHandler.getTraceId()).thenReturn(TRACE_ID);

        iojAuditRecorder.recordFindByLegacyId(legacyAppealId, found);

        AuditEventRequest req = captureSingleRecordedRequest();

        assertThat(req.eventType()).isEqualTo(AuditEventType.FIND);
        assertThat(req.getIdentifierByName(AuditIdentifierType.LEGACY_APPEAL_ID))
                .isPresent()
                .get()
                .extracting(AuditIdentifier::value)
                .isEqualTo(String.valueOf(legacyAppealId));

        Map<String, Object> payload = payload(req);
        assertThat(payload).containsEntry("path", expectedPath).containsEntry("outcome", expectedOutcome);

        assertThat(details(payload)).isEqualTo(Map.of());
    }

    private static Stream<Arguments> legacyFindCases() {
        return Stream.of(
                Arguments.of(true, AuditPath.LOCAL_HIT, AuditOutcome.SUCCESS),
                Arguments.of(false, AuditPath.LOCAL_MISS, AuditOutcome.NOT_FOUND));
    }

    @ParameterizedTest
    @MethodSource("legacyResultCases")
    void givenLegacyFoundFlag_whenRecordFindByLegacyIdMissThenLegacyResult_thenExpectedOutcomeAndPathAreRecorded(
            boolean legacyFound, AuditOutcome expectedOutcome, AuditPath expectedPath) {
        int legacyAppealId = 123;
        when(clientIdResolver.resolveOrAnonymous()).thenReturn(CLIENT_ID);
        when(traceIdHandler.getTraceId()).thenReturn(TRACE_ID);

        iojAuditRecorder.recordFindByLegacyIdMissThenLegacyResult(legacyAppealId, legacyFound);

        AuditEventRequest req = captureSingleRecordedRequest();

        assertThat(req.eventType()).isEqualTo(AuditEventType.FIND);
        assertThat(req.getIdentifierByName(AuditIdentifierType.LEGACY_APPEAL_ID))
                .isPresent();

        Map<String, Object> payload = payload(req);
        assertThat(payload).containsEntry("path", expectedPath).containsEntry("outcome", expectedOutcome);

        assertThat(details(payload)).isEqualTo(Map.of());
    }

    static Stream<Arguments> legacyResultCases() {
        return Stream.of(
                Arguments.of(true, AuditOutcome.SUCCESS, AuditPath.LOCAL_MISS_LEGACY_HIT),
                Arguments.of(false, AuditOutcome.NOT_FOUND, AuditPath.LOCAL_MISS_LEGACY_MISS));
    }

    @Test
    void givenException_whenRecordFindByLegacyIdLegacyFailure_thenFailurePathIsRecorded() {
        int legacyAppealId = 123;
        Exception e = new RuntimeException();
        when(clientIdResolver.resolveOrAnonymous()).thenReturn(CLIENT_ID);
        when(traceIdHandler.getTraceId()).thenReturn(TRACE_ID);

        iojAuditRecorder.recordFindByLegacyIdLegacyFailure(legacyAppealId, e);

        AuditEventRequest req = captureSingleRecordedRequest();

        assertThat(req.eventType()).isEqualTo(AuditEventType.FIND);
        assertThat(req.getIdentifierByName(AuditIdentifierType.LEGACY_APPEAL_ID))
                .isPresent();

        Map<String, Object> payload = payload(req);
        assertThat(payload)
                .containsEntry("path", AuditPath.LOCAL_MISS_LEGACY_FAILURE)
                .containsEntry("outcome", AuditOutcome.FAILURE);

        assertThat(details(payload)).isEqualTo(Map.of());
    }

    @Test
    void givenCreateRequest_whenRecordCreateSuccess_thenDualWriteSuccessPayloadAndIdentifiersAreRecorded() {
        UUID appealId = UUID.randomUUID();
        int legacyAppealId = 123;
        ApiCreateIojAppealRequest request = mock(ApiCreateIojAppealRequest.class);

        when(clientIdResolver.resolveOrAnonymous()).thenReturn(CLIENT_ID);
        when(traceIdHandler.getTraceId()).thenReturn(TRACE_ID);

        iojAuditRecorder.recordCreateSuccess(appealId, legacyAppealId, request);

        AuditEventRequest req = captureSingleRecordedRequest();

        assertThat(req.eventType()).isEqualTo(AuditEventType.CREATE);
        assertThat(req.getIdentifierByName(AuditIdentifierType.APPEAL_ID))
                .isPresent()
                .get()
                .extracting(AuditIdentifier::value)
                .isEqualTo(appealId.toString());

        assertThat(req.getIdentifierByName(AuditIdentifierType.LEGACY_APPEAL_ID))
                .isPresent()
                .get()
                .extracting(AuditIdentifier::value)
                .isEqualTo(String.valueOf(legacyAppealId));

        Map<String, Object> payload = payload(req);
        assertThat(payload)
                .containsEntry("path", AuditPath.DUAL_WRITE_SUCCESS)
                .containsEntry("outcome", AuditOutcome.SUCCESS);

        assertThat(details(payload)).isInstanceOf(Map.class);
    }

    @Test
    void givenCreateRequestAndException_whenRecordCreateFailure_thenDualWriteFailurePayloadAndIdentifiersAreRecorded() {
        UUID appealId = UUID.randomUUID();
        int legacyAppealId = 123;
        ApiCreateIojAppealRequest request = mock(ApiCreateIojAppealRequest.class);
        Exception e = new RuntimeException("example error");

        when(clientIdResolver.resolveOrAnonymous()).thenReturn(CLIENT_ID);
        when(traceIdHandler.getTraceId()).thenReturn(TRACE_ID);

        iojAuditRecorder.recordCreateFailure(appealId, legacyAppealId, request, e);

        AuditEventRequest req = captureSingleRecordedRequest();

        assertThat(req.eventType()).isEqualTo(AuditEventType.CREATE);
        assertThat(req.getIdentifierByName(AuditIdentifierType.APPEAL_ID)).isPresent();
        assertThat(req.getIdentifierByName(AuditIdentifierType.LEGACY_APPEAL_ID))
                .isPresent();

        Map<String, Object> payload = payload(req);
        assertThat(payload)
                .containsEntry("path", AuditPath.DUAL_WRITE_FAILURE)
                .containsEntry("outcome", AuditOutcome.FAILURE);

        assertThat(details(payload)).isInstanceOf(Map.class);
    }

    private AuditEventRequest captureSingleRecordedRequest() {
        verify(audit, times(1)).record(requestCaptor.capture());
        verify(clientIdResolver, times(1)).resolveOrAnonymous();
        verify(traceIdHandler, times(1)).getTraceId();
        return requestCaptor.getValue();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> payload(AuditEventRequest req) {
        assertThat(req.auditPayload()).isInstanceOf(Map.class);
        return (Map<String, Object>) req.auditPayload();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> details(Map<String, Object> payload) {
        Object details = payload.get("details");
        assertThat(details).isInstanceOf(Map.class);
        return (Map<String, Object>) details;
    }
}
