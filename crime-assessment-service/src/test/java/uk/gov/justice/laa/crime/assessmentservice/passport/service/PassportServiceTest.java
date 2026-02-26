package uk.gov.justice.laa.crime.assessmentservice.passport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.common.api.client.MaatCourtDataApiClient;
import uk.gov.justice.laa.crime.common.model.passported.ApiGetPassportedAssessmentResponse;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PassportServiceTest {
    @Mock
    private MaatCourtDataApiClient maatCourtDataApiClient;

    @InjectMocks
    private PassportService passportService;

    private static int LEGACY_ID = 1;

    @Test
    void givenNoResult_whenFindIsInvoked_thenReturnsEmptyOptional() {
        when(maatCourtDataApiClient.getPassportAssessment(LEGACY_ID)).thenReturn(null);

        Optional<ApiGetPassportedAssessmentResponse> passportedAssessmentResponse = passportService.find(LEGACY_ID);

        assertThat(passportedAssessmentResponse).isEmpty();
        verify(maatCourtDataApiClient).getPassportAssessment(LEGACY_ID);
        verifyNoMoreInteractions(maatCourtDataApiClient);
    }

    @Test
    void givenResult_whenFindIsInvoked_thenAppealIsReturned() {
        ApiGetPassportedAssessmentResponse passportedAssessmentResponse =
                new ApiGetPassportedAssessmentResponse().withLegacyAssessmentId(LEGACY_ID);
        when(maatCourtDataApiClient.getPassportAssessment(anyInt())).thenReturn(passportedAssessmentResponse);

        Optional<ApiGetPassportedAssessmentResponse> passportAssessment = passportService.find(LEGACY_ID);

        assertThat(passportAssessment).containsSame(passportedAssessmentResponse);
        verify(maatCourtDataApiClient).getPassportAssessment(LEGACY_ID);
        verifyNoMoreInteractions(maatCourtDataApiClient);
    }

    @Test
    void givenExceptionIsReturned_whenFindIsInvoked_thenExceptionIsRethrown() {
        RuntimeException runtimeException = new RuntimeException();

        when(maatCourtDataApiClient.getPassportAssessment(anyInt())).thenThrow(runtimeException);
        assertThatThrownBy(() -> passportService.find(LEGACY_ID)).isSameAs(runtimeException);
    }
}
