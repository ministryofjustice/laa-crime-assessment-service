package uk.gov.justice.laa.crime.assessmentservice.iojappeal;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.justice.laa.crime.assessmentservice.AssessmentServiceApplication;
import uk.gov.justice.laa.crime.assessmentservice.CrimeAssessmentTestConfiguration;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository.IojAppealRepository;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.IojAppealService;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.IojAppealDecisionReason;
import uk.gov.justice.laa.crime.enums.NewWorkReason;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.JsonPath;

@EnableWireMock
@DirtiesContext
@AutoConfigureObservability
@Import(CrimeAssessmentTestConfiguration.class)
@SpringBootTest(classes = AssessmentServiceApplication.class, webEnvironment = DEFINED_PORT)
class IojAppealIntegrationTest {

    private MockMvc mvc;

    private static final UUID APPEAL_ID = UUID.fromString("04a0d8a7-127a-44d0-bef1-d020e4ddc608");
    private static final String BEARER_TOKEN = "Bearer token";
    private static final String ENDPOINT_URL = "/api/internal/v1/ioj-appeals";
    private static final String ENDPOINT_URL_FIND = ENDPOINT_URL + "/" + APPEAL_ID;
    private static final String ENDPOINT_URL_FIND_LEGACY = ENDPOINT_URL + "/lookup-by-legacy-id";
    private static final String MAAT_API_APPEAL_URL = "/api/internal/v2/assessment/ioj-appeals";

    @InjectWireMock
    private static WireMockServer wiremock;

    @Autowired
    private IojAppealRepository iojAppealRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoSpyBean
    IojAppealService iojAppealService;

    @BeforeEach
    void setup() throws JsonProcessingException {
        stubForOAuth();
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .addFilter(springSecurityFilterChain)
                .build();
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
    }

    @Test
    void givenAppealExistsInLegacy_whenFindLegacyIojAppealIsInvoked_thenReturnsAppeal() throws Exception {
        ApiGetIojAppealResponse response = new ApiGetIojAppealResponse()
                .withAppealId(APPEAL_ID.toString())
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
    }

    @Test
    void givenValidCreateRequest_whenCreateIsInvoked_thenSuccess() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        var initialAppealCount = iojAppealRepository.count();
        var response = new ApiCreateIojAppealResponse().withLegacyAppealId(1001);

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
        String appealId = JsonPath.read(result.getResponse().getContentAsString(), "$.appealId");
        Integer legacyAppealId = JsonPath.read(result.getResponse().getContentAsString(), "$.legacyAppealId");

        // verify we've created.
        assertThat(iojAppealRepository.count()).isEqualTo(initialAppealCount + 1);
        verifySavedIds(appealId, legacyAppealId);
    }

    @Test
    void givenMaatFailure_whenCreateIsInvoked_thenNothingWritten() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        var initialAppealCount = iojAppealRepository.count();
        wiremock.stubFor(post(urlEqualTo(MAAT_API_APPEAL_URL)).willReturn(WireMock.serverError()));
        mvc.perform(MockMvcRequestBuilders.post(ENDPOINT_URL)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(APPLICATION_JSON));
        // ensure we've rolled back the DB write
        assertThat(iojAppealRepository.count()).isEqualTo(initialAppealCount);
    }

    @Test
    void givenUpdateFailure_whenCreateIsInvoked_thenAppealIsWritten() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        var initialAppealCount = iojAppealRepository.count();
        var response = new ApiCreateIojAppealResponse().withLegacyAppealId(1001);
        doThrow(new RuntimeException("Test Exception")).when(iojAppealService).save(any(IojAppealEntity.class));

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
        String appealId = JsonPath.read(result.getResponse().getContentAsString(), "$.appealId");
        Integer legacyAppealId = JsonPath.read(result.getResponse().getContentAsString(), "$.legacyAppealId");
        // verify we've created. A failure in step 3 is not currently a rollback.
        assertThat(iojAppealRepository.count()).isEqualTo(initialAppealCount + 1);
        verifySavedIds(appealId, legacyAppealId);
    }

    private void setupEntity(IojAppealEntity iojAppealEntity) {
        iojAppealRepository.save(iojAppealEntity);
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

    private void verifySavedIds(String appealId, Integer legacyAppealId) {
        assertThat(iojAppealRepository.findIojAppealByAppealId(UUID.fromString(appealId)))
                .isNotNull()
                .hasFieldOrPropertyWithValue("legacyAppealId", legacyAppealId);
        assertThat(iojAppealRepository.findIojAppealByLegacyAppealId(legacyAppealId))
                .isNotNull();
    }
}
