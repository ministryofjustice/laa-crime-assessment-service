package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.common.api.client.MaatCourtDataApiClient;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LegacyIojAppealServiceTest {

    @Mock
    private MaatCourtDataApiClient maatCourtDataApiClient;

    @InjectMocks
    private LegacyIojAppealService legacyIojAppealService;

    @Test
    void givenNoResult_whenFindIsInvoked_thenReturnsEmptyOptional() {
        int legacyAppealId = 1;

        when(maatCourtDataApiClient.getIojAppeal(legacyAppealId)).thenReturn(null);

        Optional<ApiGetIojAppealResponse> iojAppeal = legacyIojAppealService.find(legacyAppealId);

        assertThat(iojAppeal).isEmpty();
        verify(maatCourtDataApiClient).getIojAppeal(1);
        verifyNoMoreInteractions(maatCourtDataApiClient);
    }

    @Test
    void givenResult_whenFindIsInvoked_thenAppealIsReturned() {
        int legacyAppealId = 1;

        ApiGetIojAppealResponse iojAppealResponse = new ApiGetIojAppealResponse().withLegacyAppealId(legacyAppealId);
        when(maatCourtDataApiClient.getIojAppeal(anyInt())).thenReturn(iojAppealResponse);

        Optional<ApiGetIojAppealResponse> iojAppeal = legacyIojAppealService.find(legacyAppealId);

        assertThat(iojAppeal).containsSame(iojAppealResponse);
        verify(maatCourtDataApiClient).getIojAppeal(legacyAppealId);
        verifyNoMoreInteractions(maatCourtDataApiClient);
    }

    @Test
    void whenCreateIsInvoked_thenDelegatesToClientAndReturnsResponse() {
        ApiCreateIojAppealRequest request = new ApiCreateIojAppealRequest();
        ApiCreateIojAppealResponse expected = new ApiCreateIojAppealResponse();

        when(maatCourtDataApiClient.createIojAppeal(request)).thenReturn(expected);

        ApiCreateIojAppealResponse actual = legacyIojAppealService.create(request);

        assertThat(actual).isSameAs(expected);
        verify(maatCourtDataApiClient).createIojAppeal(request);
        verifyNoMoreInteractions(maatCourtDataApiClient);
    }

    @Test
    void whenRollbackIsInvoked_thenDelegatesToClient() {
        int legacyAppealId = 77;

        legacyIojAppealService.rollback(legacyAppealId);

        verify(maatCourtDataApiClient).rollbackIojAppeal(legacyAppealId);
        verifyNoMoreInteractions(maatCourtDataApiClient);
    }
}
