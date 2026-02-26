package uk.gov.justice.laa.crime.assessmentservice.passport.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.justice.laa.crime.assessmentservice.passport.service.PassportService;
import uk.gov.justice.laa.crime.common.model.passported.ApiGetPassportedAssessmentResponse;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = PassportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PassportControllerTest {
    @Autowired
    private MockMvc mockMvc;

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
}
