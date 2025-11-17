package uk.gov.justice.laa.crime.assessmentservice.iojappeal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.swagger.v3.core.util.ObjectMapperFactory;
import uk.gov.justice.laa.crime.assessmentservice.common.dto.IojAppealDTO;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.IojAppealService;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.LegacyIojAppealService;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.util.UUID;
import uk.gov.justice.laa.crime.common.model.common.ApiUserSession;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppealMetadata;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.IojAppealDecision;
import uk.gov.justice.laa.crime.enums.IojAppealDecisionReason;
import uk.gov.justice.laa.crime.enums.NewWorkReason;

import java.time.LocalDateTime;

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

    @MockitoBean
    private IojAppealService iojAppealService;

    @MockitoBean
    private LegacyIojAppealService legacyIojAppealService;

    @Autowired
    private MockMvc mockMvc;

    private static final String IOJ_APPEALS_ENDPOINT = "/api/v1/ioj-appeals";
    private static final String FIND_ENDPOINT = IOJ_APPEALS_ENDPOINT + "/{id}";
    private static final String FIND_BY_LEGACY_ID_ENDPOINT = IOJ_APPEALS_ENDPOINT + "/lookup-by-legacy-id/{id}";
    private static final int TEST_ID = 123;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.createJsonConverter();

    @Test
    void givenInvalidRequest_whenFindAppealIsInvoked_thenReturnsBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(FIND_ENDPOINT, "not-a-valid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidRequest_whenFindAppealIsInvoked_thenReturnsOkResponse() throws Exception {
        UUID appealId = UUID.randomUUID();

        when(iojAppealService.findIojAppeal(appealId))
                .thenReturn(new ApiGetIojAppealResponse().withAppealId(appealId.toString()));

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

        when(legacyIojAppealService.findIojAppeal(legacyAppealId))
                .thenReturn(new ApiGetIojAppealResponse().withLegacyAppealId(legacyAppealId));

        mockMvc.perform(MockMvcRequestBuilders.get(FIND_BY_LEGACY_ID_ENDPOINT, legacyAppealId))
                .andExpect(status().isOk());
    }

    @Test
    void givenEndpointNotImplemented_whenCreateEndpointCalledWithValidRequest_then501Error() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(IOJAPPEAL_CREATE_ENDPOINT)
    void givenEndpointNotImplemented_whenCreateAppealIsInvokedCalled_thenError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(IOJ_APPEALS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(OBJECT_MAPPER.writeValueAsString(createPopulatedValidRequest())))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void givenInvalidRequest_whenCreateEndpointCalled_then400WithErrorList() throws Exception {
        var request = new ApiCreateIojAppealRequest();
        request.setIojAppealMetadata(new IojAppealMetadata());
        request.setIojAppeal(new IojAppeal());

        mockMvc.perform(MockMvcRequestBuilders.post(IOJAPPEAL_CREATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(OBJECT_MAPPER.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400 BAD_REQUEST"))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.messageList").isArray())
                .andExpect(jsonPath("$.messageList.size()").value("9"))
                .andExpect(jsonPath("$.messageList[0]", Matchers.containsString(" is missing.")));
    }

    @Test
    void givenIncorrectMethod_whenCreateByIdEndpointCalled_thenError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.patch(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.put(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());
    }

    // helpers
    private ApiCreateIojAppealRequest createPopulatedValidRequest() {
        ApiCreateIojAppealRequest request = new ApiCreateIojAppealRequest();
        var appeal = new IojAppeal();
        appeal.setAppealDecision(IojAppealDecision.PASS);
        appeal.setAppealReason(NewWorkReason.JR);
        appeal.setAppealAssessor(IojAppealAssessor.JUDGE);
        appeal.setDecisionReason(IojAppealDecisionReason.INTERESTS_PERSON);
        appeal.setReceivedDate(LocalDateTime.now());
        appeal.setDecisionDate(LocalDateTime.now());

        var metaData = new IojAppealMetadata();
        metaData.setApplicationId("123");
        metaData.setLegacyApplicationId(456);
        var session = new ApiUserSession();
        session.setUserName("Test User");
        session.setSessionId("Test Session");
        metaData.setUserSession(session);
        metaData.setCaseManagementUnitId(789);

        request.setIojAppeal(appeal);
        request.setIojAppealMetadata(metaData);
        return request;
    }
}
