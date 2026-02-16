package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.audit.api.IojAuditRecorder;
import uk.gov.justice.laa.crime.assessmentservice.common.api.exception.AssessmentRollbackException;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IojAppealOrchestrationServiceTest {

    @Mock
    private IojAuditRecorder iojAuditRecorder;

    @Mock
    private IojAppealService iojAppealService;

    @Mock
    private LegacyIojAppealService legacyIojAppealService;

    @InjectMocks
    private IojAppealOrchestrationService service;

    @Test
    void givenLocalPresent_whenFindByAppealId_thenReturnsLocalAndAuditsPresentTrue() {
        UUID appealId = UUID.randomUUID();
        ApiGetIojAppealResponse response = new ApiGetIojAppealResponse();

        when(iojAppealService.find(appealId)).thenReturn(Optional.of(response));

        Optional<ApiGetIojAppealResponse> result = service.find(appealId);

        assertThat(result).containsSame(response);

        verify(iojAppealService).find(appealId);
        verify(iojAuditRecorder).recordFindByAppealId(appealId, true);
        verifyNoInteractions(legacyIojAppealService);
        verifyNoMoreInteractions(iojAuditRecorder, iojAppealService);
    }

    @Test
    void givenLocalEmpty_whenFindByAppealId_thenReturnsEmptyAndAuditsPresentFalse() {
        UUID appealId = UUID.randomUUID();

        when(iojAppealService.find(appealId)).thenReturn(Optional.empty());

        Optional<ApiGetIojAppealResponse> result = service.find(appealId);

        assertThat(result).isEmpty();

        verify(iojAppealService).find(appealId);
        verify(iojAuditRecorder).recordFindByAppealId(appealId, false);
        verifyNoInteractions(legacyIojAppealService);
        verifyNoMoreInteractions(iojAuditRecorder, iojAppealService);
    }

    @Test
    void givenLocalPresent_whenFindByLegacyId_thenReturnsLocalAndAuditsHit_andDoesNotCallLegacy() {
        int legacyAppealId = 123;
        ApiGetIojAppealResponse localResponse = new ApiGetIojAppealResponse();

        when(iojAppealService.find(legacyAppealId)).thenReturn(Optional.of(localResponse));

        Optional<ApiGetIojAppealResponse> result = service.find(legacyAppealId);

        assertThat(result).containsSame(localResponse);

        verify(iojAppealService).find(legacyAppealId);
        verify(iojAuditRecorder).recordFindByLegacyIdHit(legacyAppealId);
        verifyNoInteractions(legacyIojAppealService);
        verifyNoMoreInteractions(iojAuditRecorder, iojAppealService);
    }

    @Test
    void givenLocalEmptyAndLegacyPresent_whenFindByLegacyId_thenReturnsLegacyAndAuditsMissThenLegacyResultTrue() {
        int legacyAppealId = 456;
        ApiGetIojAppealResponse legacyResponse = new ApiGetIojAppealResponse();

        when(iojAppealService.find(legacyAppealId)).thenReturn(Optional.empty());
        when(legacyIojAppealService.find(legacyAppealId)).thenReturn(Optional.of(legacyResponse));

        Optional<ApiGetIojAppealResponse> result = service.find(legacyAppealId);

        assertThat(result).containsSame(legacyResponse);

        verify(iojAppealService).find(legacyAppealId);
        verify(legacyIojAppealService).find(legacyAppealId);
        verify(iojAuditRecorder).recordFindByLegacyIdMissThenLegacyResult(legacyAppealId, true);
        verifyNoMoreInteractions(iojAuditRecorder, iojAppealService, legacyIojAppealService);
    }

    @Test
    void givenLocalEmptyAndLegacyEmpty_whenFindByLegacyId_thenReturnsEmptyAndAuditsMissThenLegacyResultFalse() {
        int legacyAppealId = 789;

        when(iojAppealService.find(legacyAppealId)).thenReturn(Optional.empty());
        when(legacyIojAppealService.find(legacyAppealId)).thenReturn(Optional.empty());

        Optional<ApiGetIojAppealResponse> result = service.find(legacyAppealId);

        assertThat(result).isEmpty();

        verify(iojAppealService).find(legacyAppealId);
        verify(legacyIojAppealService).find(legacyAppealId);
        verify(iojAuditRecorder).recordFindByLegacyIdMissThenLegacyResult(legacyAppealId, false);
        verifyNoMoreInteractions(iojAuditRecorder, iojAppealService, legacyIojAppealService);
    }

    @Test
    void givenLocalEmptyAndLegacyThrows_whenFindByLegacyId_thenAuditsFailureAndRethrows() {
        int legacyAppealId = 42;
        RuntimeException exception = new RuntimeException("legacy down");

        when(iojAppealService.find(legacyAppealId)).thenReturn(Optional.empty());
        when(legacyIojAppealService.find(legacyAppealId)).thenThrow(exception);

        assertThatThrownBy(() -> service.find(legacyAppealId)).isSameAs(exception);

        verify(iojAppealService).find(legacyAppealId);
        verify(legacyIojAppealService).find(legacyAppealId);
        verify(iojAuditRecorder).recordFindByLegacyIdLegacyFailure(legacyAppealId, exception);
        verifyNoMoreInteractions(iojAuditRecorder, iojAppealService, legacyIojAppealService);
    }

    @Test
    void givenHappyPath_whenCreateIojAppeal_thenSetsLegacyIdSavesAuditsSuccessAndReturnsResponse() {
        ApiCreateIojAppealRequest request = new ApiCreateIojAppealRequest();

        UUID appealId = UUID.randomUUID();
        IojAppealEntity entity = mock(IojAppealEntity.class);
        when(entity.getAppealId()).thenReturn(appealId);

        ApiCreateIojAppealResponse legacyCreated = new ApiCreateIojAppealResponse().withLegacyAppealId(999);

        when(iojAppealService.create(request)).thenReturn(entity);
        when(legacyIojAppealService.create(request)).thenReturn(legacyCreated);

        ApiCreateIojAppealResponse result = service.createIojAppeal(request);

        verify(entity).setLegacyAppealId(999);
        verify(iojAppealService).save(entity);
        verify(iojAuditRecorder).recordCreateSuccess(appealId, 999, request);

        assertThat(result.getAppealId()).isEqualTo(appealId.toString());
        assertThat(result.getLegacyAppealId()).isEqualTo(999);

        verify(legacyIojAppealService, never()).rollback(anyInt());
        verify(iojAppealService, never()).delete(any());

        verifyNoMoreInteractions(iojAuditRecorder, iojAppealService, legacyIojAppealService, entity);
    }

    @Test
    void givenSaveThrows_whenCreateIojAppeal_thenRollsBackDeletesAuditsFailureAndThrowsException() {
        ApiCreateIojAppealRequest request = new ApiCreateIojAppealRequest();

        UUID appealId = UUID.randomUUID();
        IojAppealEntity entity = mock(IojAppealEntity.class);
        when(entity.getAppealId()).thenReturn(appealId);

        ApiCreateIojAppealResponse legacyCreated = new ApiCreateIojAppealResponse().withLegacyAppealId(1001);

        when(iojAppealService.create(request)).thenReturn(entity);
        when(legacyIojAppealService.create(request)).thenReturn(legacyCreated);

        RuntimeException exception = new RuntimeException("db down");
        doThrow(exception).when(iojAppealService).save(entity);

        assertThatThrownBy(() -> service.createIojAppeal(request))
                .isInstanceOf(AssessmentRollbackException.class)
                .hasMessageContaining("Error linking appealId")
                .hasMessageContaining(appealId.toString())
                .hasMessageContaining("1001")
                .hasMessageContaining("db down");

        // rollback + delete
        verify(entity).setLegacyAppealId(1001);
        verify(iojAppealService).delete(entity);
        verify(legacyIojAppealService).rollback(1001);

        // failure audit uses legacy id + exception
        verify(iojAuditRecorder).recordCreateFailure(appealId, 1001, request, exception);

        verifyNoMoreInteractions(iojAuditRecorder, iojAppealService, legacyIojAppealService, entity);
    }
}
