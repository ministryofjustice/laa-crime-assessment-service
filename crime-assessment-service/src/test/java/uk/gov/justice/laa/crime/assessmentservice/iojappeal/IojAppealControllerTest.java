package uk.gov.justice.laa.crime.assessmentservice.iojappeal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.swagger.v3.core.util.ObjectMapperFactory;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = IojAppealController.class)
@AutoConfigureMockMvc(addFilters = false)
class IojAppealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String IOJAPPEAL_ENDPOINT = "/api/v1/iojappeal";
    private static final String IOJAPPEAL_FIND_ENDPOINT = IOJAPPEAL_ENDPOINT + "/{id}";
    private static final String IOJAPPEAL_CREATE_ENDPOINT = IOJAPPEAL_ENDPOINT + "/";
    private static final String IOJAPPEAL_LEGACY_ID_ENDPOINT = IOJAPPEAL_ENDPOINT + "/lookup-by-legacy-id/{id}";
    private static final Integer TEST_ID = 123;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.createJsonConverter();

    @Test
    void givenEndpointNotImplemented_whenGetByIdEndpointCalled_thenError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(IOJAPPEAL_FIND_ENDPOINT, TEST_ID))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void givenEndpointNotImplemented_whenCreateEndpointCalled_thenError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(IOJAPPEAL_CREATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(OBJECT_MAPPER.writeValueAsString(new ApiCreateIojAppealRequest())))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void givenIncorrectMethod_whenCreateByIdEndpointCalled_thenError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(IOJAPPEAL_FIND_ENDPOINT, TEST_ID))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.patch(IOJAPPEAL_FIND_ENDPOINT, TEST_ID))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.put(IOJAPPEAL_FIND_ENDPOINT, TEST_ID))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void givenEndpointNotImplemented_whenGetByLegacyIdEndpointCalled_thenError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(IOJAPPEAL_LEGACY_ID_ENDPOINT, TEST_ID))
                .andExpect(status().isNotImplemented());
        mockMvc.perform(MockMvcRequestBuilders.post(IOJAPPEAL_LEGACY_ID_ENDPOINT, TEST_ID))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.delete(IOJAPPEAL_LEGACY_ID_ENDPOINT, TEST_ID))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.patch(IOJAPPEAL_LEGACY_ID_ENDPOINT, TEST_ID))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.put(IOJAPPEAL_LEGACY_ID_ENDPOINT, TEST_ID))
                .andExpect(status().isMethodNotAllowed());
    }
}
