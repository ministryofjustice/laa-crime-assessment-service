package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.common.client.MaatCourtDataApiClient;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper.IojAppealMapper;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository.IojAppealRepository;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LegacyIojAppealServiceTest {

    @Mock
    private IojAppealMapper iojAppealMapper;

    @Mock
    private IojAppealRepository iojAppealRepository;

    @Mock
    private MaatCourtDataApiClient maatCourtDataApiClient;

    @InjectMocks
    private LegacyIojAppealService legacyIojAppealService;

    @Test
    void givenAppealNotFound_whenLegacyFindIsInvoked_thenReturnsNull() {
        int legacyAppealId = 1;

        when(iojAppealRepository.findIojAppealByLegacyAppealId(legacyAppealId)).thenReturn(null);
        when(maatCourtDataApiClient.getIojAppeal(legacyAppealId)).thenReturn(null);

        ApiGetIojAppealResponse iojAppeal = legacyIojAppealService.find(legacyAppealId);

        assertThat(iojAppeal).isNull();
    }

    @Test
    void givenAppealNotFoundInAssessmentServiceButFoundInMAAT_whenLegacyFindIsInvoked_thenReturnsNull() {
        int legacyAppealId = 1;

        ApiGetIojAppealResponse iojAppealResponse = new ApiGetIojAppealResponse().withLegacyAppealId(legacyAppealId);

        when(iojAppealRepository.findIojAppealByLegacyAppealId(legacyAppealId)).thenReturn(null);
        when(maatCourtDataApiClient.getIojAppeal(legacyAppealId)).thenReturn(iojAppealResponse);

        ApiGetIojAppealResponse iojAppeal = legacyIojAppealService.find(legacyAppealId);

        assertThat(iojAppeal).isEqualTo(iojAppealResponse);
    }

    @Test
    void givenAppealIsFoundInAssessmentServiceDb_whenLegacyFind() {
        int legacyAppealId = 1;

        IojAppealEntity iojAppealEntity =
                IojAppealEntity.builder().legacyAppealId(legacyAppealId).build();

        ApiGetIojAppealResponse iojAppealResponse = new ApiGetIojAppealResponse().withLegacyAppealId(legacyAppealId);

        when(iojAppealRepository.findIojAppealByLegacyAppealId(legacyAppealId)).thenReturn(iojAppealEntity);
        when(iojAppealMapper.mapEntityToDTO(iojAppealEntity)).thenReturn(iojAppealResponse);

        ApiGetIojAppealResponse iojAppeal = legacyIojAppealService.find(legacyAppealId);

        assertThat(iojAppeal).isEqualTo(iojAppealResponse);
    }
}
