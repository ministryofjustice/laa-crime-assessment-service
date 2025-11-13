package uk.gov.justice.laa.crime.assessmentservice.iojappeal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.swagger.v3.core.util.ObjectMapperFactory;
import uk.gov.justice.laa.crime.assessmentservice.common.dto.IojAppealDTO;

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

    private static final String IOJ_APPEALS_ENDPOINT = "/api/v1/ioj-appeals";
    private static final String FIND_ENDPOINT = IOJ_APPEALS_ENDPOINT + "/{id}";
    private static final String FIND_BY_LEGACY_ID_ENDPOINT = IOJ_APPEALS_ENDPOINT + "/lookup-by-legacy-id/{id}";
    private static final int TEST_ID = 123;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.createJsonConverter();

    @Test
    void givenEndpointNotImplemented_whenGetByIdEndpointCalled_thenError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(FIND_ENDPOINT, TEST_ID)).andExpect(status().isNotImplemented());
    }

    @Test
    void givenEndpointNotImplemented_whenCreateEndpointCalled_thenError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(IOJ_APPEALS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(OBJECT_MAPPER.writeValueAsString(
                                IojAppealDTO.builder().build())))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void givenIncorrectMethod_whenCreateByIdEndpointCalled_thenError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.patch(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.put(FIND_ENDPOINT, TEST_ID)).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void givenEndpointNotImplemented_whenGetByLegacyIdEndpointCalled_thenError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(FIND_BY_LEGACY_ID_ENDPOINT, TEST_ID))
                .andExpect(status().isNotImplemented());
        mockMvc.perform(MockMvcRequestBuilders.post(FIND_BY_LEGACY_ID_ENDPOINT, TEST_ID))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.delete(FIND_BY_LEGACY_ID_ENDPOINT, TEST_ID))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.patch(FIND_BY_LEGACY_ID_ENDPOINT, TEST_ID))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(MockMvcRequestBuilders.put(FIND_BY_LEGACY_ID_ENDPOINT, TEST_ID))
                .andExpect(status().isMethodNotAllowed());
    }
}
