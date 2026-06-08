package uk.gov.justice.laa.crime.assessmentservice.passport.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.justice.laa.crime.assessmentservice.passport.validator.ApiCreatePassportedAssessmentRequestValidator.ERROR_LAST_SIGN_ON_DATE_EMPTY;

import uk.gov.justice.laa.crime.assessmentservice.passport.enums.ApiCreatePassportedAssessmentRequestFields;
import uk.gov.justice.laa.crime.assessmentservice.passport.service.PassportService;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;
import uk.gov.justice.laa.crime.common.model.passported.ApiGetPassportedAssessmentResponse;
import uk.gov.justice.laa.crime.enums.BenefitType;
import uk.gov.justice.laa.crime.error.ErrorMessage;
import uk.gov.justice.laa.crime.error.ProblemDetailError;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;
import uk.gov.justice.laa.crime.util.ProblemDetailUtil;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = PassportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PassportControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PassportService passportService;

    @MockitoBean
    private TraceIdHandler traceIdHandler;

    private static final String PASSPORT_ENDPOINT = "/api/internal/v1/passport";
    private static final String FIND_ENDPOINT = PASSPORT_ENDPOINT + "/lookup-by-legacy-id/{legacyId}";

    @Test
    void givenInvalidRequest_whenFindLegacyAppealIsInvoked_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(FIND_ENDPOINT, "not-a-valid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidRequest_whenFindLegacyAppealIsInvoked_thenReturnsOkResponse() throws Exception {
        int legacyId = 1;

        when(passportService.find(legacyId))
                .thenReturn(
                        Optional.ofNullable(new ApiGetPassportedAssessmentResponse().withLegacyAssessmentId(legacyId)));

        mockMvc.perform(MockMvcRequestBuilders.get(FIND_ENDPOINT, legacyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legacyAssessmentId").value(legacyId));
    }

    @Test
    void givenNullSignOnDateForJSA_whenCreateIsInvoked_thenReturnsBadRequest() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreatePassportedAssessmentRequest();
        request.getPassportedAssessment().getDeclaredBenefit().setBenefitType(BenefitType.JSA);
        request.getPassportedAssessment().getDeclaredBenefit().setLastSignOnDate(null);

        var result = mockMvc.perform(MockMvcRequestBuilders.post(PASSPORT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        var errorResponse =
                ProblemDetailUtil.parseProblemDetailJson(result.getResponse().getContentAsString());
        var optionalOfExtension = ProblemDetailUtil.getErrorExtension(errorResponse);
        assertThat(optionalOfExtension).isPresent();
        var extension = optionalOfExtension.get();
        List<ErrorMessage> errorMessages = extension.errors();

        assertThat(errorResponse)
                .hasFieldOrPropertyWithValue("title", HttpStatus.BAD_REQUEST.getReasonPhrase())
                .hasFieldOrPropertyWithValue("status", 400)
                .hasFieldOrPropertyWithValue("detail", ProblemDetailError.VALIDATION_FAILURE.defaultDetail())
                .hasFieldOrPropertyWithValue("instance", new URI("/api/internal/v1/passport"));

        assertThat(extension).hasFieldOrPropertyWithValue("code", ProblemDetailError.VALIDATION_FAILURE.code());

        var expectedErrorMessage = new ErrorMessage(
                ApiCreatePassportedAssessmentRequestFields.LAST_SIGN_ON_DATE.getName(), ERROR_LAST_SIGN_ON_DATE_EMPTY);
        assertThat(errorMessages).isNotNull().hasSize(1).contains(expectedErrorMessage);

        verifyNoInteractions(passportService);
    }
}
