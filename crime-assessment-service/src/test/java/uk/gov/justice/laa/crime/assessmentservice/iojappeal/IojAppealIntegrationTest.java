package uk.gov.justice.laa.crime.assessmentservice.iojappeal;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.justice.laa.crime.assessmentservice.WiremockIntegrationTest;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.IojAuditRecorder;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository.IojAppealRepository;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.IojAppealService;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestConstants;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.IojAppealDecisionReason;
import uk.gov.justice.laa.crime.enums.NewWorkReason;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.JsonPath;

@DirtiesContext
@AutoConfigureMockMvc
@AutoConfigureObservability
class IojAppealIntegrationTest extends WiremockIntegrationTest {

    private static final String BEARER_TOKEN = "Bearer token";
    private static final String ENDPOINT_URL = "/api/internal/v1/ioj-appeals";
    private static final String ENDPOINT_URL_FIND = ENDPOINT_URL + "/" + TestConstants.APPEAL_ID;
    private static final String ENDPOINT_URL_FIND_LEGACY = ENDPOINT_URL + "/lookup-by-legacy-id";
    private static final String ENDPOINT_URL_ROLLBACK = ENDPOINT_URL + "/rollback/";
    private static final String MAAT_API_APPEAL_URL = "/api/internal/v2/assessment/ioj-appeals";
    private static final String MAAT_API_APPEAL_ROLLBACK_URL =
            MAAT_API_APPEAL_URL + "/rollback/" + TestConstants.LEGACY_APPEAL_ID;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private IojAppealRepository iojAppealRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IojAuditRecorder iojAuditRecorder;

    @MockitoSpyBean
    IojAppealService iojAppealService;

    @MockitoSpyBean
    private TraceIdHandler traceIdHandler;

    @BeforeEach
    void setup() throws JsonProcessingException {
        stubForOAuth();
        Mockito.reset(iojAuditRecorder);
        iojAppealRepository.deleteAll();
    }

    @Test
    void givenUnauthorisedRequest_whenFindIojAppealIsInvoked_thenFailsWithUnauthorisedAccess() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL_FIND)).andExpect(status().isUnauthorized());
    }

    @Test
    void givenAppealDoesNotExist_whenFindIojAppealIsInvoked_thenFailsWithNotFound() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL_FIND).header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isNotFound());
        Mockito.verify(iojAuditRecorder).recordFindByAppealId(UUID.fromString(TestConstants.APPEAL_ID), false);
    }

    @Test
    void givenAppealExists_whenFindIojAppealIsInvoked_thenReturnsAppeal() throws Exception {
        IojAppealEntity iojAppealEntity = TestDataBuilder.buildIojAppealEntity();
        setupEntity(iojAppealEntity);

        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL + "/" + iojAppealEntity.getAppealId())
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.appealId")
                        .value(iojAppealEntity.getAppealId().toString()))
                .andExpect(jsonPath("$.legacyAppealId").value(iojAppealEntity.getLegacyAppealId()))
                .andExpect(jsonPath("$.receivedDate").value("2025-02-01"))
                .andExpect(jsonPath("$.appealAssessor").value(iojAppealEntity.getAppealAssessor()))
                .andExpect(jsonPath("$.appealSuccessful").value(iojAppealEntity.isAppealSuccessful()))
                .andExpect(jsonPath("$.decisionReason").value(IojAppealDecisionReason.DAMAGE_TO_REPUTATION.getCode()))
                .andExpect(jsonPath("$.notes").value(iojAppealEntity.getNotes()))
                .andExpect(jsonPath("$.decisionDate").value("2025-02-08"))
                .andExpect(jsonPath("$.caseManagementUnitId").value(iojAppealEntity.getCaseManagementUnitId()));

        Mockito.verify(iojAuditRecorder).recordFindByAppealId(iojAppealEntity.getAppealId(), true);
    }

    @Test
    void givenLocalMissAndLegacyApiFailure_whenFindByLegacyId_thenReturns5xx() throws Exception {

        wiremock.stubFor(get(urlEqualTo(MAAT_API_APPEAL_URL + "/" + TestConstants.LEGACY_APPEAL_ID))
                .willReturn(WireMock.serverError()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"example failure\"}")));

        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL_FIND_LEGACY + "/" + TestConstants.LEGACY_APPEAL_ID)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().is5xxServerError());

        verify(getRequestedFor(urlEqualTo(MAAT_API_APPEAL_URL + "/" + TestConstants.LEGACY_APPEAL_ID)));
        Mockito.verify(iojAuditRecorder)
                .recordFindByLegacyIdLegacyFailure(eq(TestConstants.LEGACY_APPEAL_ID), any(Exception.class));
    }

    @Test
    void givenAppealExistsInAssessmentService_whenFindLegacyIojAppealIsInvoked_thenReturnsAppeal() throws Exception {
        IojAppealEntity iojAppealEntity = TestDataBuilder.buildIojAppealEntity();
        setupEntity(iojAppealEntity);

        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL_FIND_LEGACY + "/" + iojAppealEntity.getLegacyAppealId())
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.appealId")
                        .value(iojAppealEntity.getAppealId().toString()))
                .andExpect(jsonPath("$.legacyAppealId").value(iojAppealEntity.getLegacyAppealId()))
                .andExpect(jsonPath("$.receivedDate").value("2025-02-01"))
                .andExpect(jsonPath("$.appealAssessor").value(iojAppealEntity.getAppealAssessor()))
                .andExpect(jsonPath("$.appealSuccessful").value(iojAppealEntity.isAppealSuccessful()))
                .andExpect(jsonPath("$.decisionReason").value(IojAppealDecisionReason.DAMAGE_TO_REPUTATION.getCode()))
                .andExpect(jsonPath("$.notes").value(iojAppealEntity.getNotes()))
                .andExpect(jsonPath("$.decisionDate").value("2025-02-08"))
                .andExpect(jsonPath("$.caseManagementUnitId").value(iojAppealEntity.getCaseManagementUnitId()));

        Mockito.verify(iojAuditRecorder).recordFindByLegacyId(iojAppealEntity.getLegacyAppealId(), true);
    }

    @Test
    void givenAppealExistsInLegacy_whenFindLegacyIojAppealIsInvoked_thenReturnsAppeal() throws Exception {
        ApiGetIojAppealResponse response = new ApiGetIojAppealResponse()
                .withAppealId(TestConstants.APPEAL_ID)
                .withLegacyAppealId(223)
                .withReceivedDate(LocalDate.of(2025, 2, 1))
                .withAppealReason(NewWorkReason.NEW)
                .withAppealAssessor(IojAppealAssessor.CASEWORKER)
                .withAppealSuccessful(true)
                .withDecisionReason(IojAppealDecisionReason.DAMAGE_TO_REPUTATION)
                .withNotes("Passing IoJ Appeal")
                .withDecisionDate(LocalDate.of(2025, 2, 8))
                .withCaseManagementUnitId(44);

        wiremock.stubFor(get(urlEqualTo(MAAT_API_APPEAL_URL + "/223"))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", String.valueOf(APPLICATION_JSON))
                        .withBody(objectMapper.writeValueAsString(response))));

        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL_FIND_LEGACY + "/" + response.getLegacyAppealId())
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.appealId").value(response.getAppealId()))
                .andExpect(jsonPath("$.legacyAppealId").value(response.getLegacyAppealId()))
                .andExpect(jsonPath("$.receivedDate").value("2025-02-01"))
                .andExpect(jsonPath("$.appealAssessor")
                        .value(response.getAppealAssessor().toString()))
                .andExpect(jsonPath("$.appealSuccessful").value(response.getAppealSuccessful()))
                .andExpect(jsonPath("$.decisionReason").value(IojAppealDecisionReason.DAMAGE_TO_REPUTATION.getCode()))
                .andExpect(jsonPath("$.notes").value(response.getNotes()))
                .andExpect(jsonPath("$.decisionDate").value("2025-02-08"))
                .andExpect(jsonPath("$.caseManagementUnitId").value(response.getCaseManagementUnitId()));

        Mockito.verify(iojAuditRecorder).recordFindByLegacyIdMissThenLegacyResult(response.getLegacyAppealId(), true);
    }

    @Test
    void givenAppealDoesNotExist_whenRollbackAppealIsInvoked_thenFailsWithNotFound() throws Exception {
        mvc.perform(MockMvcRequestBuilders.patch(ENDPOINT_URL_ROLLBACK).header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenAppealExists_whenRollbackAppealIsInvoked_thenReturnsSuccess() throws Exception {
        IojAppealEntity iojAppealEntity = TestDataBuilder.buildIojAppealEntity();
        iojAppealEntity = setupEntity(iojAppealEntity);

        mvc.perform(MockMvcRequestBuilders.patch(ENDPOINT_URL_ROLLBACK
                                + iojAppealEntity.getAppealId().toString())
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk());

        verify(patchRequestedFor(urlEqualTo(MAAT_API_APPEAL_ROLLBACK_URL)));

        Mockito.verify(iojAuditRecorder)
                .recordRollbackSuccess(eq(iojAppealEntity.getAppealId()), eq(iojAppealEntity.getLegacyAppealId()));
    }

    @Test
    void givenValidCreateRequest_whenCreateIsInvoked_thenSuccess() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        var initialAppealCount = iojAppealRepository.count();
        var response = new ApiCreateIojAppealResponse().withLegacyAppealId(TestConstants.LEGACY_APPEAL_ID);

        wiremock.stubFor(post(urlEqualTo(MAAT_API_APPEAL_URL))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", String.valueOf(APPLICATION_JSON))
                        .withBody(objectMapper.writeValueAsString(response))));

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post(ENDPOINT_URL)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        UUID appealId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.appealId"));
        Integer legacyAppealId = JsonPath.read(result.getResponse().getContentAsString(), "$.legacyAppealId");

        assertThat(iojAppealRepository.count()).isEqualTo(initialAppealCount + 1);
        assertThat(iojAppealRepository.findIojAppealByAppealId(appealId))
                .isNotNull()
                .hasFieldOrPropertyWithValue("legacyAppealId", legacyAppealId);

        Mockito.verify(iojAuditRecorder)
                .recordCreateSuccess(eq(appealId), eq(legacyAppealId), any(ApiCreateIojAppealRequest.class));
    }

    @Test
    void givenMaatFailure_whenCreateIsInvoked_thenNothingWritten() throws Exception {
        var testTrace = "Test Trace";
        doReturn(testTrace).when(traceIdHandler).getTraceId();

        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        var initialAppealCount = iojAppealRepository.count();
        wiremock.stubFor(post(urlEqualTo(MAAT_API_APPEAL_URL)).willReturn(WireMock.serverError()));
        mvc.perform(MockMvcRequestBuilders.post(ENDPOINT_URL)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.errors.traceId").value(testTrace));
        // ensure we've rolled back the DB write
        assertThat(iojAppealRepository.count()).isEqualTo(initialAppealCount);
    }

    @Test
    void givenUpdateFailure_whenCreateIsInvoked_thenAppealIsRolledBack() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        var initialAppealCount = iojAppealRepository.count();
        var response = new ApiCreateIojAppealResponse().withLegacyAppealId(TestConstants.LEGACY_APPEAL_ID);
        doThrow(new RuntimeException("Test Exception")).when(iojAppealService).save(any(IojAppealEntity.class));

        wiremock.stubFor(post(urlEqualTo(MAAT_API_APPEAL_URL))
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", String.valueOf(APPLICATION_JSON))
                        .withBody(objectMapper.writeValueAsString(response))));

        wiremock.stubFor(patch(urlEqualTo(MAAT_API_APPEAL_ROLLBACK_URL)).willReturn(WireMock.ok()));

        mvc.perform(MockMvcRequestBuilders.post(ENDPOINT_URL)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(555))
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andReturn();

        verify(patchRequestedFor(urlEqualTo(MAAT_API_APPEAL_ROLLBACK_URL)));
        assertThat(iojAppealRepository.count()).isEqualTo(initialAppealCount);
        Mockito.verify(iojAuditRecorder)
                .recordCreateFailure(
                        any(UUID.class),
                        any(Integer.class),
                        any(ApiCreateIojAppealRequest.class),
                        any(Exception.class));
    }

    private IojAppealEntity setupEntity(IojAppealEntity iojAppealEntity) {
        return iojAppealRepository.save(iojAppealEntity);
    }

    private void stubForOAuth() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> token =
                Map.of("expires_in", 3600, "token_type", "Bearer", "access_token", java.util.UUID.randomUUID());

        wiremock.stubFor(post("/oauth2/token")
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", String.valueOf(APPLICATION_JSON))
                        .withBody(mapper.writeValueAsString(token))));
    }
}
