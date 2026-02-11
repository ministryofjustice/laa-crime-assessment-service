package uk.gov.justice.laa.crime.assessmentservice.iojappeal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.justice.laa.crime.assessmentservice.iojappeal.controller.IojAppealController;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.IojAppealDualWriteService;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppealMetadata;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;

import java.util.Optional;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = IojAppealController.class)
@AutoConfigureMockMvc(addFilters = false)
class IojAppealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IojAppealDualWriteService iojAppealDualWriteService;

    @MockitoBean
    private TraceIdHandler traceIdHandler;

    private static final String IOJ_APPEALS_ENDPOINT = "/api/internal/v1/ioj-appeals";
    private static final String FIND_ENDPOINT = IOJ_APPEALS_ENDPOINT + "/{id}";
    private static final String FIND_BY_LEGACY_ID_ENDPOINT = IOJ_APPEALS_ENDPOINT + "/lookup-by-legacy-id/{id}";
    private static final int TEST_ID = 123;

    @Test
    void givenInvalidRequest_whenFindAppealIsInvoked_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(FIND_ENDPOINT, "not-a-valid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidRequest_whenFindAppealIsInvoked_thenReturnsOkResponse() throws Exception {
        UUID appealId = UUID.randomUUID();

        when(iojAppealDualWriteService.find(appealId))
                .thenReturn(Optional.of(new ApiGetIojAppealResponse().withAppealId(appealId.toString())));

        mockMvc.perform(MockMvcRequestBuilders.get(FIND_ENDPOINT, appealId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void givenInvalidRequest_whenFindLegacyAppealIsInvoked_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(FIND_BY_LEGACY_ID_ENDPOINT, "not-a-valid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidRequest_whenFindLegacyAppealIsInvoked_thenReturnsOkResponse() throws Exception {
        int legacyAppealId = 1;

        when(iojAppealDualWriteService.find(legacyAppealId))
                .thenReturn(Optional.ofNullable(new ApiGetIojAppealResponse().withLegacyAppealId(legacyAppealId)));

        mockMvc.perform(MockMvcRequestBuilders.get(FIND_BY_LEGACY_ID_ENDPOINT, legacyAppealId))
                .andExpect(status().isOk());
    }

    @Test
    void givenEndpoint_whenCreateEndpointCalledWithValidRequest_thenOkResponseWithIds() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        ApiCreateIojAppealResponse response = new ApiCreateIojAppealResponse()
                .withAppealId(UUID.randomUUID().toString())
                .withLegacyAppealId(1);

        when(iojAppealDualWriteService.createIojAppeal(request)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post(IOJ_APPEALS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appealId").value(response.getAppealId()))
                .andExpect(jsonPath("$.legacyAppealId").value(response.getLegacyAppealId()));
    }

    @Test
    void givenInvalidRequest_whenCreateEndpointCalled_thenBadRequestWithErrorList() throws Exception {
        var request = new ApiCreateIojAppealRequest();
        request.setIojAppealMetadata(new IojAppealMetadata());
        request.setIojAppeal(new IojAppeal());
        mockMvc.perform(MockMvcRequestBuilders.post(IOJ_APPEALS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation Failure"))
                .andExpect(jsonPath("$.instance").value("/api/internal/v1/ioj-appeals"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.code").value("VALIDATION_FAILURE"))
                .andExpect(jsonPath("$.errors.errors").isArray())
                .andExpect(jsonPath("$.errors.errors.size()").value(10))
                .andExpect(jsonPath("$.errors.errors[0].message", Matchers.containsString(" is missing.")));
    }

    @Test
    void givenIncorrectMethod_whenCreateByIdEndpointCalled_thenError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.patch(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.put(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());
    }
}
