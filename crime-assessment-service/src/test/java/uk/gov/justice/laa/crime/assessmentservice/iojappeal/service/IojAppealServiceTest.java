package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper.IojAppealMapper;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository.IojAppealRepository;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IojAppealServiceTest {

    @Mock
    private IojAppealMapper iojAppealMapper;

    @Mock
    private IojAppealRepository iojAppealRepository;

    @InjectMocks
    private IojAppealService iojAppealService;

    @Test
    void givenAppealNotFound_whenFindIsInvoked_thenReturnsEmptyOptional() {
        UUID appealId = UUID.fromString("04a0d8a7-127a-44d0-bef1-d020e4ddc608");

        when(iojAppealRepository.findIojAppealByAppealId(appealId)).thenReturn(null);

        Optional<ApiGetIojAppealResponse> iojAppeal = iojAppealService.find(appealId);

        assertThat(iojAppeal).isEmpty();
        verify(iojAppealRepository).findIojAppealByAppealId(appealId);
        verifyNoMoreInteractions(iojAppealRepository);
    }

    @Test
    void givenAppealIsFound_whenFind() {
        UUID appealId = UUID.fromString("04a0d8a7-127a-44d0-bef1-d020e4ddc608");

        IojAppealEntity iojAppealEntity =
                IojAppealEntity.builder().appealId(appealId).build();

        ApiGetIojAppealResponse iojAppealResponse = new ApiGetIojAppealResponse().withAppealId(appealId.toString());

        when(iojAppealRepository.findIojAppealByAppealId(appealId)).thenReturn(iojAppealEntity);
        when(iojAppealMapper.mapEntityToDTO(iojAppealEntity)).thenReturn(iojAppealResponse);

        Optional<ApiGetIojAppealResponse> iojAppeal = iojAppealService.find(appealId);

        assertThat(iojAppeal).containsSame(iojAppealResponse);
    }

    @Test
    void givenAppealNotFound_whenFindByAppealId_thenReturnsEmpty_andDoesNotMap() {
        UUID appealId = UUID.fromString("04a0d8a7-127a-44d0-bef1-d020e4ddc608");
        when(iojAppealRepository.findIojAppealByAppealId(appealId)).thenReturn(null);

        Optional<ApiGetIojAppealResponse> result = iojAppealService.find(appealId);

        assertThat(result).isEmpty();
        verify(iojAppealRepository).findIojAppealByAppealId(appealId);
        verifyNoInteractions(iojAppealMapper);
        verifyNoMoreInteractions(iojAppealRepository);
    }

    @Test
    void givenAppealFound_whenFindByAppealId_thenMapsAndReturnsDto() {
        UUID appealId = UUID.fromString("04a0d8a7-127a-44d0-bef1-d020e4ddc608");

        IojAppealEntity entity = IojAppealEntity.builder().appealId(appealId).build();
        ApiGetIojAppealResponse dto = new ApiGetIojAppealResponse().withAppealId(appealId.toString());

        when(iojAppealRepository.findIojAppealByAppealId(appealId)).thenReturn(entity);
        when(iojAppealMapper.mapEntityToDTO(entity)).thenReturn(dto);

        Optional<ApiGetIojAppealResponse> result = iojAppealService.find(appealId);

        assertThat(result).containsSame(dto);
        verify(iojAppealRepository).findIojAppealByAppealId(appealId);
        verify(iojAppealMapper).mapEntityToDTO(entity);
        verifyNoMoreInteractions(iojAppealRepository, iojAppealMapper);
    }

    @Test
    void givenAppealNotFound_whenFindByLegacyAppealId_thenReturnsEmpty_andDoesNotMap() {
        int legacyAppealId = 123;
        when(iojAppealRepository.findIojAppealByLegacyAppealId(legacyAppealId)).thenReturn(null);

        Optional<ApiGetIojAppealResponse> result = iojAppealService.find(legacyAppealId);

        assertThat(result).isEmpty();
        verify(iojAppealRepository).findIojAppealByLegacyAppealId(legacyAppealId);
        verifyNoInteractions(iojAppealMapper);
        verifyNoMoreInteractions(iojAppealRepository);
    }

    @Test
    void givenAppealFound_whenFindByLegacyAppealId_thenMapsAndReturnsDto() {
        int legacyAppealId = 123;

        IojAppealEntity entity =
                IojAppealEntity.builder().legacyAppealId(legacyAppealId).build();
        ApiGetIojAppealResponse dto = new ApiGetIojAppealResponse().withLegacyAppealId(legacyAppealId);

        when(iojAppealRepository.findIojAppealByLegacyAppealId(legacyAppealId)).thenReturn(entity);
        when(iojAppealMapper.mapEntityToDTO(entity)).thenReturn(dto);

        Optional<ApiGetIojAppealResponse> result = iojAppealService.find(legacyAppealId);

        assertThat(result).containsSame(dto);
        verify(iojAppealRepository).findIojAppealByLegacyAppealId(legacyAppealId);
        verify(iojAppealMapper).mapEntityToDTO(entity);
        verifyNoMoreInteractions(iojAppealRepository, iojAppealMapper);
    }

    @Test
    void whenCreate_thenMapsRequestToEntity_savesEntity_andReturnsSameEntity() {
        ApiCreateIojAppealRequest request = new ApiCreateIojAppealRequest();

        IojAppealEntity mappedEntity = IojAppealEntity.builder().build();
        when(iojAppealMapper.mapCreateAppealRequestToEntity(request)).thenReturn(mappedEntity);

        IojAppealEntity result = iojAppealService.create(request);

        assertThat(result).isSameAs(mappedEntity);

        verify(iojAppealMapper).mapCreateAppealRequestToEntity(request);
        verify(iojAppealRepository).save(mappedEntity);
        verifyNoMoreInteractions(iojAppealMapper, iojAppealRepository);
    }

    @Test
    void whenSave_thenDelegatesToRepository_andReturnsRepositoryResult() {
        IojAppealEntity input = IojAppealEntity.builder().build();
        IojAppealEntity saved = IojAppealEntity.builder().build();

        when(iojAppealRepository.save(input)).thenReturn(saved);

        IojAppealEntity result = iojAppealService.save(input);

        assertThat(result).isSameAs(saved);
        verify(iojAppealRepository).save(input);
        verifyNoInteractions(iojAppealMapper);
        verifyNoMoreInteractions(iojAppealRepository);
    }

    @Test
    void whenDelete_thenDelegatesToRepository() {
        IojAppealEntity entity = IojAppealEntity.builder().build();

        iojAppealService.delete(entity);

        verify(iojAppealRepository).delete(entity);
        verifyNoInteractions(iojAppealMapper);
        verifyNoMoreInteractions(iojAppealRepository);
    }
}
