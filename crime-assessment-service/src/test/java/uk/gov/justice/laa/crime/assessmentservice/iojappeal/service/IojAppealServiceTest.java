package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper.IojAppealMapper;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository.IojAppealRepository;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IojAppealServiceTest {

    @Mock
    private IojAppealMapper iojAppealMapper;

    @Mock
    private IojAppealRepository iojAppealRepository;

    @InjectMocks
    private IojAppealService iojAppealService;

    @Test
    void givenAppealNotFound_whenFindIojAppealIsInvoked_thenReturnsNull() {
        UUID appealId = UUID.fromString("04a0d8a7-127a-44d0-bef1-d020e4ddc608");

        when(iojAppealRepository.findIojAppealByAppealId(appealId)).thenReturn(null);

        ApiGetIojAppealResponse iojAppeal = iojAppealService.findIojAppeal(appealId);

        assertThat(iojAppeal).isNull();
    }

    @Test
    void givenAppealIsFound_whenFindIojAppealIsInvoked_thenReturnsAppeal() {
        UUID appealId = UUID.fromString("04a0d8a7-127a-44d0-bef1-d020e4ddc608");

        IojAppealEntity iojAppealEntity =
                IojAppealEntity.builder().appealId(appealId).build();

        ApiGetIojAppealResponse iojAppealResponse = new ApiGetIojAppealResponse().withAppealId(appealId.toString());

        when(iojAppealRepository.findIojAppealByAppealId(appealId)).thenReturn(iojAppealEntity);
        when(iojAppealMapper.mapEntityToDTO(iojAppealEntity)).thenReturn(iojAppealResponse);

        ApiGetIojAppealResponse iojAppeal = iojAppealService.findIojAppeal(appealId);

        assertThat(iojAppeal).isEqualTo(iojAppealResponse);
    }
}
