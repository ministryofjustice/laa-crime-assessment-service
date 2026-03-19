package uk.gov.justice.laa.crime.assessmentservice.iojappeal;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator.ApiCreateIojAppealRequestValidator.ERROR_FIELD_IS_MISSING;

import uk.gov.justice.laa.crime.assessmentservice.common.api.advice.ApiError;
import uk.gov.justice.laa.crime.assessmentservice.common.api.exception.RequestedObjectNotFoundException;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.controller.IojAppealController;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.enums.ApiCreateIojAppealRequestFields;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.IojAppealOrchestrationService;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiRollbackIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppealMetadata;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;

import java.util.UUID;

import org.hamcrest.Matchers;
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

@WebMvcTest(controllers = IojAppealController.class)
@AutoConfigureMockMvc(addFilters = false)
class IojAppealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IojAppealOrchestrationService iojAppealOrchestrationService;

    @MockitoBean
    private TraceIdHandler traceIdHandler;

    private static final int TEST_ID = 123;
    private static final String IOJ_APPEALS_ENDPOINT = "/api/internal/v1/ioj-appeals";
    private static final String FIND_ENDPOINT = IOJ_APPEALS_ENDPOINT + "/{id}";
    private static final String FIND_BY_LEGACY_ID_ENDPOINT = IOJ_APPEALS_ENDPOINT + "/lookup-by-legacy-id/{id}";
    private static final String ROLLBACK_ENDPOINT = IOJ_APPEALS_ENDPOINT + "/{id}/rollback";

    @Test
    void givenInvalidRequest_whenFindAppealIsInvoked_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(FIND_ENDPOINT, "not-a-valid-id"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(iojAppealOrchestrationService);
    }

    @Test
    void givenValidRequest_whenFindAppealIsInvoked_thenReturnsOkResponse() throws Exception {
        UUID appealId = UUID.randomUUID();

        when(iojAppealOrchestrationService.findOrThrow(appealId))
                .thenReturn(new ApiGetIojAppealResponse().withAppealId(appealId.toString()));

        mockMvc.perform(MockMvcRequestBuilders.get(FIND_ENDPOINT, appealId.toString()))
                .andExpect(status().isOk());

        verify(iojAppealOrchestrationService).findOrThrow(appealId);
    }

    @Test
    void givenInvalidRequest_whenFindLegacyAppealIsInvoked_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(FIND_BY_LEGACY_ID_ENDPOINT, "not-a-valid-id"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(iojAppealOrchestrationService);
    }

    @Test
    void givenValidRequest_whenFindLegacyAppealIsInvoked_thenReturnsOkResponse() throws Exception {
        int legacyAppealId = 1;

        when(iojAppealOrchestrationService.findOrThrow(legacyAppealId))
                .thenReturn(new ApiGetIojAppealResponse().withLegacyAppealId(legacyAppealId));

        mockMvc.perform(MockMvcRequestBuilders.get(FIND_BY_LEGACY_ID_ENDPOINT, legacyAppealId))
                .andExpect(status().isOk());

        verify(iojAppealOrchestrationService).findOrThrow(legacyAppealId);
    }

    @Test
    void givenValidRequest_whenCreateIsInvoked_thenReturnsOkResponse() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        ApiCreateIojAppealResponse response = new ApiCreateIojAppealResponse()
                .withAppealId(UUID.randomUUID().toString())
                .withLegacyAppealId(1);

        when(iojAppealOrchestrationService.createIojAppeal(request)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post(IOJ_APPEALS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appealId").value(response.getAppealId()))
                .andExpect(jsonPath("$.legacyAppealId").value(response.getLegacyAppealId()));

        verify(iojAppealOrchestrationService).createIojAppeal(request);
    }

    @Test
    void givenBeanValidationFailure_whenCreateIsInvoked_thenReturnsBadRequest() throws Exception {
        var request = new ApiCreateIojAppealRequest();
        request.setIojAppealMetadata(new IojAppealMetadata());
        request.setIojAppeal(new IojAppeal());

        mockMvc.perform(MockMvcRequestBuilders.post(IOJ_APPEALS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(ApiError.VALIDATION_FAILURE.defaultDetail()))
                .andExpect(jsonPath("$.instance").value("/api/internal/v1/ioj-appeals"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.code").value(ApiError.VALIDATION_FAILURE.code()))
                .andExpect(jsonPath("$.errors.errors").isArray())
                .andExpect(jsonPath("$.errors.errors.length()").value(Matchers.greaterThan(0)));

        verifyNoInteractions(iojAppealOrchestrationService);
    }

    @Test
    void givenCustomValidationFailure_whenCreateIsInvoked_thenReturnsBadRequest() throws Exception {
        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        request.getIojAppealMetadata().setLegacyApplicationId(null);

        mockMvc.perform(MockMvcRequestBuilders.post(IOJ_APPEALS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(ApiError.VALIDATION_FAILURE.defaultDetail()))
                .andExpect(jsonPath("$.errors.code").value(ApiError.VALIDATION_FAILURE.code()))
                .andExpect(jsonPath("$.errors.errors").isArray())
                .andExpect(jsonPath(
                        "$.errors.errors[?(@.field == '%s')].message"
                                .formatted(ApiCreateIojAppealRequestFields.LEGACY_APPLICATION_ID.getName()),
                        Matchers.hasItem(String.format(
                                ERROR_FIELD_IS_MISSING,
                                ApiCreateIojAppealRequestFields.LEGACY_APPLICATION_ID.getName()))));

        verifyNoInteractions(iojAppealOrchestrationService);
    }

    @Test
    void givenUnsupportedMethod_whenFindEndpointInvoked_thenReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.delete(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.patch(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(iojAppealOrchestrationService);
    }

    @Test
    void givenInvalidRequest_whenRollbackAppealIsInvoked_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(ROLLBACK_ENDPOINT, "not-a-valid-id"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(iojAppealOrchestrationService);
    }

    @Test
    void givenUnknownIojAppeal_whenRollbackAppealIsInvoked_thenReturnsNotFound() throws Exception {
        UUID appealId = UUID.randomUUID();

        String exceptionMessage = "IOJ appeal not found for appealId: " + appealId;

        doThrow(new RequestedObjectNotFoundException(exceptionMessage))
                .when(iojAppealOrchestrationService)
                .rollbackIojAppeal(appealId);

        mockMvc.perform(MockMvcRequestBuilders.post(ROLLBACK_ENDPOINT, appealId.toString()))
                .andExpect(status().isNotFound());

        verify(iojAppealOrchestrationService).rollbackIojAppeal(appealId);
    }

    @Test
    void givenUnsuccessfulRollback_whenRollbackAppealIsInvoked_thenReturnsErrorResponse() throws Exception {
        UUID appealId = UUID.randomUUID();

        ApiRollbackIojAppealResponse rollbackResponse = new ApiRollbackIojAppealResponse()
                .withAppealId(appealId.toString())
                .withLegacyAppealId(TEST_ID)
                .withRollbackSuccessful(false);

        when(iojAppealOrchestrationService.rollbackIojAppeal(appealId)).thenReturn(rollbackResponse);

        mockMvc.perform(MockMvcRequestBuilders.post(ROLLBACK_ENDPOINT, appealId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appealId").value(appealId.toString()))
                .andExpect(jsonPath("$.legacyAppealId").value(TEST_ID))
                .andExpect(jsonPath("$.rollbackSuccessful").value(false));

        verify(iojAppealOrchestrationService).rollbackIojAppeal(appealId);
    }

    @Test
    void givenValidRequest_whenRollbackAppealIsInvoked_thenReturnsOkResponse() throws Exception {
        UUID appealId = UUID.randomUUID();

        ApiRollbackIojAppealResponse rollbackResponse = new ApiRollbackIojAppealResponse()
                .withAppealId(appealId.toString())
                .withLegacyAppealId(TEST_ID)
                .withRollbackSuccessful(true);

        when(iojAppealOrchestrationService.rollbackIojAppeal(appealId)).thenReturn(rollbackResponse);

        mockMvc.perform(MockMvcRequestBuilders.post(ROLLBACK_ENDPOINT, appealId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appealId").value(appealId.toString()))
                .andExpect(jsonPath("$.legacyAppealId").value(TEST_ID))
                .andExpect(jsonPath("$.rollbackSuccessful").value(true));

        verify(iojAppealOrchestrationService).rollbackIojAppeal(appealId);
    }
}
