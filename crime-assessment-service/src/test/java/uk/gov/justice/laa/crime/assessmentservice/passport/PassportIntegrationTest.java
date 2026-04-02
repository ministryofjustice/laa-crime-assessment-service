package uk.gov.justice.laa.crime.assessmentservice.passport;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.justice.laa.crime.assessmentservice.WiremockIntegrationTest;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;
import uk.gov.justice.laa.crime.common.model.passported.ApiGetPassportedAssessmentResponse;
import uk.gov.justice.laa.crime.common.model.passported.DeclaredBenefit;
import uk.gov.justice.laa.crime.enums.NewWorkReason;
import uk.gov.justice.laa.crime.enums.PassportAssessmentDecision;
import uk.gov.justice.laa.crime.enums.PassportAssessmentDecisionReason;
import uk.gov.justice.laa.crime.enums.ReviewType;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability
class PassportIntegrationTest extends WiremockIntegrationTest {
    private static final String BEARER_TOKEN = "Bearer token";
    private static final String PASSPORT_ENDPOINT = "/api/internal/v1/passport";
    private static final String FIND_ENDPOINT = PASSPORT_ENDPOINT + "/lookup-by-legacy-id/";
    private static final String MAAT_API_PASSPORT_URL = "/api/internal/v2/assessment/passport-assessments";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws JsonProcessingException {
        stubForOAuth();
    }

    @Test
    void givenUnauthorisedRequest_whenFindPassportIsInvoked_thenFailsWithUnauthorisedAccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(FIND_ENDPOINT + 1)).andExpect(status().isUnauthorized());
    }

    @Test
    void givenPassportAssessmentDoesNotExist_whenFindPassportIsInvoked_thenFailsWithNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(FIND_ENDPOINT + 1).header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenPassportAssessmentExists_whenFindPassportIsInvoked_thenReturnsPassportAssessment() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.of(2026, 3, 2, 15, 51, 12, 932911);

        DeclaredBenefit declaredBenefit = TestDataBuilder.buildDeclaredBenefit();

        ApiGetPassportedAssessmentResponse response = new ApiGetPassportedAssessmentResponse()
                .withLegacyAssessmentId(123)
                .withUsn(555)
                .withAssessmentDate(localDateTime)
                .withAssessmentReason(NewWorkReason.NEW)
                .withReviewType(ReviewType.ER)
                .withDeclaredUnder18(false)
                .withDeclaredBenefit(declaredBenefit)
                .withAssessmentDecision(PassportAssessmentDecision.PASS)
                .withDecisionReason(PassportAssessmentDecisionReason.IN_CUSTODY)
                .withNotes("notes");

        wiremock.stubFor(get(urlEqualTo(MAAT_API_PASSPORT_URL + "/123"))
                .willReturn(WireMock.ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.toString())
                        .withBody(objectMapper.writeValueAsString(response))));

        mockMvc.perform(MockMvcRequestBuilders.get(FIND_ENDPOINT + response.getLegacyAssessmentId())
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.legacyAssessmentId").value(response.getLegacyAssessmentId()))
                .andExpect(jsonPath("$.usn").value(response.getUsn()))
                .andExpect(jsonPath("$.assessmentDate")
                        .value(response.getAssessmentDate().toString()))
                .andExpect(jsonPath("$.assessmentReason")
                        .value(response.getAssessmentReason().getCode()))
                .andExpect(
                        jsonPath("$.reviewType").value(response.getReviewType().getCode()))
                .andExpect(jsonPath("$.declaredUnder18").value(response.getDeclaredUnder18()))
                .andExpect(jsonPath("$.declaredBenefit.benefitType")
                        .value(response.getDeclaredBenefit().getBenefitType().getCode()))
                .andExpect(jsonPath("$.declaredBenefit.benefitRecipient")
                        .value(response.getDeclaredBenefit()
                                .getBenefitRecipient()
                                .toString()))
                .andExpect(jsonPath("$.declaredBenefit.lastSignOnDate")
                        .value(response.getDeclaredBenefit().getLastSignOnDate().toString()))
                .andExpect(jsonPath("$.declaredBenefit.legacyPartnerId")
                        .value(response.getDeclaredBenefit().getLegacyPartnerId()))
                .andExpect(jsonPath("$.assessmentDecision")
                        .value(response.getAssessmentDecision().getCode()))
                .andExpect(jsonPath("$.decisionReason")
                        .value(response.getDecisionReason().getConfirmation()))
                .andExpect(jsonPath("$.notes").value(response.getNotes()));
    }

    @Test
    void givenPassportAssessmentRequest_whenCreatePassportIsInvoked_thenReturnsPassportAssessment() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreatePassportedAssessmentRequest();
        var maatResponse = TestDataBuilder.buildValidPopulatedCreatePassportedAssessmentResponse();

        wiremock.stubFor(post(urlEqualTo(MAAT_API_PASSPORT_URL))
                .willReturn(WireMock.ok()
                        .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.toString())
                        .withBody(objectMapper.writeValueAsString(maatResponse))));

        mockMvc.perform(MockMvcRequestBuilders.post(PASSPORT_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    void givenPassportAssessmentRequest_whenCreatePassportIsInvokedAndLegacyFails_thenReturnedError() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreatePassportedAssessmentRequest();
        wiremock.stubFor(post(urlEqualTo(MAAT_API_PASSPORT_URL)).willReturn(WireMock.serverError()));
        mockMvc.perform(MockMvcRequestBuilders.post(PASSPORT_ENDPOINT)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
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
