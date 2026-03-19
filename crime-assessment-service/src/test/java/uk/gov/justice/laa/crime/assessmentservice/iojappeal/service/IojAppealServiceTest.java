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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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

    private UUID appealId;

    @BeforeEach
    void setup() {
        appealId = UUID.fromString("04a0d8a7-127a-44d0-bef1-d020e4ddc608");
    }

    @Test
    void givenAppealNotFound_whenFindByAppealId_thenReturnsEmpty_andDoesNotMap() {
        when(iojAppealRepository.findByAppealIdAndRolledBackAtIsNull(appealId)).thenReturn(Optional.empty());

        Optional<ApiGetIojAppealResponse> result = iojAppealService.find(appealId);

        assertThat(result).isEmpty();
        verify(iojAppealRepository).findByAppealIdAndRolledBackAtIsNull(appealId);
        verifyNoInteractions(iojAppealMapper);
        verifyNoMoreInteractions(iojAppealRepository);
    }

    @Test
    void givenAppealFound_whenFindByAppealId_thenMapsAndReturnsDto() {
        IojAppealEntity entity = IojAppealEntity.builder().appealId(appealId).build();
        ApiGetIojAppealResponse dto = new ApiGetIojAppealResponse().withAppealId(appealId.toString());

        when(iojAppealRepository.findByAppealIdAndRolledBackAtIsNull(appealId)).thenReturn(Optional.of(entity));
        when(iojAppealMapper.mapEntityToDTO(entity)).thenReturn(dto);

        Optional<ApiGetIojAppealResponse> result = iojAppealService.find(appealId);

        assertThat(result).containsSame(dto);
        verify(iojAppealRepository).findByAppealIdAndRolledBackAtIsNull(appealId);
        verify(iojAppealMapper).mapEntityToDTO(entity);
        verifyNoMoreInteractions(iojAppealRepository, iojAppealMapper);
    }

    @Test
    void givenAppealNotFound_whenFindByLegacyAppealId_thenReturnsEmpty_andDoesNotMap() {
        int legacyAppealId = 123;
        when(iojAppealRepository.findByLegacyAppealIdAndRolledBackAtIsNull(legacyAppealId))
                .thenReturn(Optional.empty());

        Optional<ApiGetIojAppealResponse> result = iojAppealService.find(legacyAppealId);

        assertThat(result).isEmpty();
        verify(iojAppealRepository).findByLegacyAppealIdAndRolledBackAtIsNull(legacyAppealId);
        verifyNoInteractions(iojAppealMapper);
        verifyNoMoreInteractions(iojAppealRepository);
    }

    @Test
    void givenAppealFound_whenFindByLegacyAppealId_thenMapsAndReturnsDto() {
        int legacyAppealId = 123;

        IojAppealEntity entity =
                IojAppealEntity.builder().legacyAppealId(legacyAppealId).build();
        ApiGetIojAppealResponse dto = new ApiGetIojAppealResponse().withLegacyAppealId(legacyAppealId);

        when(iojAppealRepository.findByLegacyAppealIdAndRolledBackAtIsNull(legacyAppealId))
                .thenReturn(Optional.of(entity));
        when(iojAppealMapper.mapEntityToDTO(entity)).thenReturn(dto);

        Optional<ApiGetIojAppealResponse> result = iojAppealService.find(legacyAppealId);

        assertThat(result).containsSame(dto);
        verify(iojAppealMapper).mapEntityToDTO(entity);
        verify(iojAppealRepository).findByLegacyAppealIdAndRolledBackAtIsNull(legacyAppealId);
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

    @Test
    void givenAppealId_whenFindEntityIsInvoked_thenDelegatesToRepository() {
        IojAppealEntity entity = IojAppealEntity.builder().appealId(appealId).build();

        when(iojAppealRepository.findByAppealId(appealId)).thenReturn(Optional.of(entity));

        Optional<IojAppealEntity> result = iojAppealService.findEntity(appealId);

        assertThat(result).containsSame(entity);
        verify(iojAppealRepository).findByAppealId(appealId);
        verifyNoInteractions(iojAppealMapper);
        verifyNoMoreInteractions(iojAppealRepository);
    }

    @Test
    void givenLegacyAppealId_whenAppealHasNotBeenRolledBack_thenReturnsFalse() {
        int legacyAppealId = 123;

        when(iojAppealRepository.existsByLegacyAppealIdAndRolledBackAtIsNotNull(legacyAppealId))
                .thenReturn(false);

        boolean result = iojAppealService.hasBeenRolledBack(legacyAppealId);

        assertThat(result).isFalse();
        verify(iojAppealRepository).existsByLegacyAppealIdAndRolledBackAtIsNotNull(legacyAppealId);
        verifyNoMoreInteractions(iojAppealRepository);
        verifyNoInteractions(iojAppealMapper);
    }

    @Test
    void givenLegacyAppealId_whenAppealHasBeenRolledBack_thenReturnsTrue() {
        int legacyAppealId = 123;

        when(iojAppealRepository.existsByLegacyAppealIdAndRolledBackAtIsNotNull(legacyAppealId))
                .thenReturn(true);

        boolean result = iojAppealService.hasBeenRolledBack(legacyAppealId);

        assertThat(result).isTrue();
        verify(iojAppealRepository).existsByLegacyAppealIdAndRolledBackAtIsNotNull(legacyAppealId);
        verifyNoInteractions(iojAppealMapper);
        verifyNoMoreInteractions(iojAppealRepository);
    }

    @Test
    void givenEntity_whenMarkRolledBackIsInvoked_thenSetsRolledBackAtToNowAndSaves() {
        Instant before = Instant.now();

        IojAppealEntity entity = IojAppealEntity.builder().build();
        iojAppealService.markRolledBack(entity);

        Instant after = Instant.now();

        assertThat(entity.getRolledBackAt()).isBetween(before, after);
        verify(iojAppealRepository).save(entity);
        verifyNoInteractions(iojAppealMapper);
        verifyNoMoreInteractions(iojAppealRepository);
    }
}
