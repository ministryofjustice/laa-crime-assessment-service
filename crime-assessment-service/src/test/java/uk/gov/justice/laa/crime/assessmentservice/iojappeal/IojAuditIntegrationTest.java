package uk.gov.justice.laa.crime.assessmentservice.iojappeal;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.justice.laa.crime.assessmentservice.WiremockIntegrationTest;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventType;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditOutcome;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditPath;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.entity.IojAuditEventEntity;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.repository.IojAuditEventRepository;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository.IojAppealRepository;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.IojAppealService;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestConstants;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.IojAppealDecisionReason;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

@AutoConfigureMockMvc
class IojAuditIntegrationTest extends WiremockIntegrationTest {

    private static final String BEARER_TOKEN = "Bearer token";
    private static final String ENDPOINT_URL = "/api/internal/v1/ioj-appeals";
    private static final String ENDPOINT_URL_FIND_LEGACY = ENDPOINT_URL + "/lookup-by-legacy-id";
    private static final String MAAT_API_APPEAL_URL = "/api/internal/v2/assessment/ioj-appeals";
    private static final String ENDPOINT_URL_CREATE = ENDPOINT_URL;
    private static final String MAAT_API_APPEAL_ROLLBACK_URL =
            MAAT_API_APPEAL_URL + "/rollback/" + TestConstants.LEGACY_APPEAL_ID;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private IojAppealRepository iojAppealRepository;

    @Autowired
    private IojAuditEventRepository iojAuditEventRepository;

    @MockitoSpyBean
    private IojAppealService iojAppealService;

    @BeforeEach
    void setup() throws JsonProcessingException {
        stubForOAuth();
        iojAppealRepository.deleteAll();
        iojAuditEventRepository.deleteAll();
    }

    @Test
    void givenLocalMiss_whenFindByAppealId_thenAuditRecordIsPersisted() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL + "/" + TestConstants.APPEAL_ID)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isNotFound());

        // No id is stored if there is a miss, search by event type instead
        IojAuditEventEntity event = awaitAuditEvent(
                () -> iojAuditEventRepository.findIojAuditEventEntitiesByEventType(AuditEventType.FIND));

        assertThat(event).isNotNull();
        assertThat(event.getAuditPayload().path("path").asText()).isEqualTo(AuditPath.LOCAL_MISS.toString());
        assertThat(event.getAuditPayload().path("outcome").asText()).isEqualTo(AuditOutcome.NOT_FOUND.toString());
        assertThat(event.getAuditPayload().path("details").path("requestedId").asText())
                .contains(TestConstants.APPEAL_ID);
    }

    @Test
    void givenLocalHit_whenFindByLegacyId_thenAuditRecordIsPersisted() throws Exception {
        IojAppealEntity entity = TestDataBuilder.buildIojAppealEntity();
        entity.setLegacyAppealId(TestConstants.LEGACY_APPEAL_ID);
        iojAppealRepository.save(entity);

        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL_FIND_LEGACY + "/" + TestConstants.LEGACY_APPEAL_ID)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk());

        IojAuditEventEntity event =
                awaitAuditEvent(() -> iojAuditEventRepository.findIojAuditEventEntitiesByLegacyAppealId(
                        TestConstants.LEGACY_APPEAL_ID.longValue()));

        assertThat(event.getAuditPayload().path("outcome").asText()).isEqualTo(AuditOutcome.SUCCESS.toString());
        assertThat(event.getAuditPayload().path("path").asText()).isEqualTo(AuditPath.LOCAL_HIT.toString());
    }

    @Test
    void givenLocalMiss_whenFindByLegacyIdAndLegacyReturns200_thenAuditRecordIsPersisted() throws Exception {
        ApiGetIojAppealResponse response = new ApiGetIojAppealResponse()
                .withAppealId(TestConstants.APPEAL_ID)
                .withLegacyAppealId(TestConstants.LEGACY_APPEAL_ID)
                .withReceivedDate(LocalDate.of(2025, 2, 1))
                .withAppealAssessor(IojAppealAssessor.CASEWORKER)
                .withAppealSuccessful(true)
                .withDecisionReason(IojAppealDecisionReason.DAMAGE_TO_REPUTATION)
                .withDecisionDate(LocalDate.of(2025, 2, 8))
                .withCaseManagementUnitId(44);

        wiremock.stubFor(get(urlEqualTo(MAAT_API_APPEAL_URL + "/" + TestConstants.LEGACY_APPEAL_ID))
                .willReturn(okJson(objectMapper.writeValueAsString(response))));

        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL_FIND_LEGACY + "/" + TestConstants.LEGACY_APPEAL_ID)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk());

        IojAuditEventEntity event =
                awaitAuditEvent(() -> iojAuditEventRepository.findIojAuditEventEntitiesByLegacyAppealId(
                        TestConstants.LEGACY_APPEAL_ID.longValue()));

        assertThat(event.getAuditPayload().path("outcome").asText()).isEqualTo(AuditOutcome.SUCCESS.toString());
        assertThat(event.getAuditPayload().path("path").asText()).isEqualTo(AuditPath.LOCAL_MISS_LEGACY_HIT.toString());
    }

    @Test
    void givenLocalMiss_whenFindByLegacyIdAndLegacyReturns404_thenAuditRecordIsPersisted() throws Exception {

        wiremock.stubFor(get(urlEqualTo(MAAT_API_APPEAL_URL + "/" + TestConstants.LEGACY_APPEAL_ID))
                .willReturn(WireMock.notFound()));

        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL_FIND_LEGACY + "/" + TestConstants.LEGACY_APPEAL_ID)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isNotFound());

        IojAuditEventEntity event =
                awaitAuditEvent(() -> iojAuditEventRepository.findIojAuditEventEntitiesByLegacyAppealId(
                        TestConstants.LEGACY_APPEAL_ID.longValue()));

        assertThat(event).isNotNull();
        assertThat(event.getLegacyAppealId()).isEqualTo(TestConstants.LEGACY_APPEAL_ID.longValue());
        assertThat(event.getEventType()).isEqualTo(AuditEventType.FIND);
        assertThat(event.getAuditPayload().path("outcome").asText()).isEqualTo(AuditOutcome.NOT_FOUND.toString());
        assertThat(event.getAuditPayload().path("path").asText())
                .isEqualTo(AuditPath.LOCAL_MISS_LEGACY_MISS.toString());
    }

    @Test
    void givenLocalMiss_whenFindByLegacyIdAndLegacyApiFails_thenAuditRecordIsPersisted() throws Exception {
        wiremock.stubFor(get(urlEqualTo(MAAT_API_APPEAL_URL + "/" + TestConstants.LEGACY_APPEAL_ID))
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"example failure\"}")));

        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL_FIND_LEGACY + "/" + TestConstants.LEGACY_APPEAL_ID)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().is5xxServerError());

        IojAuditEventEntity event =
                awaitAuditEvent(() -> iojAuditEventRepository.findIojAuditEventEntitiesByLegacyAppealId(
                        TestConstants.LEGACY_APPEAL_ID.longValue()));

        assertThat(event).isNotNull();
        assertThat(event.getLegacyAppealId()).isEqualTo(TestConstants.LEGACY_APPEAL_ID.longValue());
        assertThat(event.getAuditPayload().path("outcome").asText()).isEqualTo(AuditOutcome.FAILURE.toString());
        assertThat(event.getAuditPayload().path("path").asText())
                .isEqualTo(AuditPath.LOCAL_MISS_LEGACY_FAILURE.toString());
    }

    @Test
    void givenAppealExists_whenFindByAppealIdHit_thenAuditRecordIsPersisted() throws Exception {
        IojAppealEntity entity = TestDataBuilder.buildIojAppealEntity();
        iojAppealRepository.save(entity);

        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL + "/" + entity.getAppealId())
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk());

        IojAuditEventEntity event = awaitAuditEvent(
                () -> iojAuditEventRepository.findIojAuditEventEntitiesByAppealId((entity.getAppealId())));

        assertThat(event).isNotNull();
        assertThat(event.getAppealId()).isEqualTo(entity.getAppealId());
        assertThat(event.getAuditPayload().path("outcome").asText()).isEqualTo(AuditOutcome.SUCCESS.toString());
        assertThat(event.getAuditPayload().path("path").asText()).isEqualTo(AuditPath.LOCAL_HIT.toString());
    }

    @Test
    void givenValidCreateRequest_whenCreateSucceeds_thenAuditCreateSuccessIsPersisted() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();

        var maatResponse = new ApiCreateIojAppealResponse().withLegacyAppealId(TestConstants.LEGACY_APPEAL_ID);

        wiremock.stubFor(post(urlEqualTo(MAAT_API_APPEAL_URL))
                .willReturn(okJson(objectMapper.writeValueAsString(maatResponse))));

        mvc.perform(MockMvcRequestBuilders.post(ENDPOINT_URL_CREATE)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        IojAuditEventEntity event = awaitAuditEvent(
                () -> iojAuditEventRepository.findIojAuditEventEntitiesByEventType(AuditEventType.CREATE));

        assertThat(event).isNotNull();
        assertThat(event.getEventType()).isEqualTo(AuditEventType.CREATE);
        assertThat(event.getAuditPayload().path("outcome").asText()).isEqualTo(AuditOutcome.SUCCESS.toString());
        assertThat(event.getAuditPayload().path("path").asText()).isEqualTo(AuditPath.DUAL_WRITE_SUCCESS.toString());
        assertThat(event.getLegacyAppealId()).isEqualTo(TestConstants.LEGACY_APPEAL_ID.longValue());
    }

    @Test
    void givenLocalUpdateFailure_whenCreateInvoked_thenAuditCreateFailureIsPersisted() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();

        // Force failure at the "link legacyAppealId then save" step
        doThrow(new RuntimeException("Test Exception")).when(iojAppealService).save(any(IojAppealEntity.class));

        var maatResponse = new ApiCreateIojAppealResponse().withLegacyAppealId(TestConstants.LEGACY_APPEAL_ID);

        wiremock.stubFor(post(urlEqualTo(MAAT_API_APPEAL_URL))
                .willReturn(okJson(objectMapper.writeValueAsString(maatResponse))));

        // rollback endpoint succeeds
        wiremock.stubFor(patch(urlEqualTo(MAAT_API_APPEAL_ROLLBACK_URL)).willReturn(WireMock.ok()));

        mvc.perform(MockMvcRequestBuilders.post(ENDPOINT_URL_CREATE)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(555));

        verify(patchRequestedFor(urlEqualTo(MAAT_API_APPEAL_ROLLBACK_URL)));

        IojAuditEventEntity event = awaitAuditEvent(
                () -> iojAuditEventRepository.findIojAuditEventEntitiesByEventType(AuditEventType.CREATE));

        assertThat(event).isNotNull();
        assertThat(event.getEventType()).isEqualTo(AuditEventType.CREATE);
        assertThat(event.getAuditPayload().path("outcome").asText()).isEqualTo(AuditOutcome.FAILURE.toString());
        assertThat(event.getAuditPayload().path("path").asText()).isEqualTo(AuditPath.DUAL_WRITE_FAILURE.toString());
    }

    private <T> T awaitAuditEvent(Supplier<T> supplier) {
        AtomicReference<T> ref = new AtomicReference<>();

        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            T value = supplier.get();
            assertThat(value).isNotNull();
            ref.set(value);
        });

        return ref.get();
    }

    private void stubForOAuth() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> token = Map.of(
                "expires_in",
                3600,
                "token_type",
                "Bearer",
                "access_token",
                UUID.randomUUID().toString());

        wiremock.stubFor(post("/oauth2/token")
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", String.valueOf(APPLICATION_JSON))
                        .withBody(mapper.writeValueAsString(token))));
    }
}
