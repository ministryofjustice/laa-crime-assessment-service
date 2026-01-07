package uk.gov.justice.laa.crime.assessmentservice.iojappeal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.common.exception.AssessmentRollbackException;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestConstants;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IojAppealDualWriteServiceTest {

    @Mock
    private IojAppealService iojAppealService;

    @Mock
    private LegacyIojAppealService legacyIojAppealService;

    @InjectMocks
    private IojAppealDualWriteService iojAppealDualWriteService;

    @Test
    void givenValidRequest_whenCreateIojAppealIsInvoked_thenIojIsCreatedAndLinked() {
        ApiCreateIojAppealRequest request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        ApiCreateIojAppealResponse response = TestDataBuilder.buildValidPopulatedCreateIojAppealResponse();
        IojAppealEntity entity = TestDataBuilder.buildIojAppealEntity();

        when(iojAppealService.create(any(ApiCreateIojAppealRequest.class))).thenReturn(entity);
        when(legacyIojAppealService.create(any(ApiCreateIojAppealRequest.class)))
                .thenReturn(response);

        IojAppealEntity result = iojAppealDualWriteService.createIojAppeal(request);

        assertThat(result.getLegacyAppealId()).isEqualTo(TestConstants.LEGACY_APPEAL_ID);
        verify(iojAppealService).save(entity);
    }

    @Test
    void givenExceptionDuringLinking_whenCreateIojAppealIsInvoked_thenIojIsRolledBack() {
        ApiCreateIojAppealRequest request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        ApiCreateIojAppealResponse response = TestDataBuilder.buildValidPopulatedCreateIojAppealResponse();
        IojAppealEntity entity = TestDataBuilder.buildIojAppealEntity(true);

        when(iojAppealService.create(any(ApiCreateIojAppealRequest.class))).thenReturn(entity);
        when(legacyIojAppealService.create(any(ApiCreateIojAppealRequest.class)))
                .thenReturn(response);
        when(iojAppealService.save(any(IojAppealEntity.class)))
                .thenThrow(new IllegalArgumentException("Test exception."));

        assertThatThrownBy(() -> iojAppealDualWriteService.createIojAppeal(request))
                .isInstanceOf(AssessmentRollbackException.class)
                .hasMessage(String.format(
                        "Error linking appealId %s to legacyAppealId %s, creation has been rolled back: %s",
                        TestConstants.APPEAL_ID, TestConstants.LEGACY_APPEAL_ID, "Test exception."));
        verify(legacyIojAppealService).rollback(TestConstants.LEGACY_APPEAL_ID);
        verify(iojAppealService).delete(entity);
    }
}
