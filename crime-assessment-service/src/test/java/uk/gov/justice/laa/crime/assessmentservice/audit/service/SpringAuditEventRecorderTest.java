package uk.gov.justice.laa.crime.assessmentservice.audit.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventRequest;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.listener.AuditEventPublished;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.service.SpringAuditEventRecorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class SpringAuditEventRecorderTest {

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private SpringAuditEventRecorder recorder;

    @Captor
    private ArgumentCaptor<AuditEventPublished> eventCaptor;

    @Test
    void givenRequest_whenRecordIsInvoked_thenPublishesAuditEventPublishedWithRequest() {
        AuditEventRequest request = mock(AuditEventRequest.class);

        recorder.record(request);

        verify(publisher).publishEvent(eventCaptor.capture());

        AuditEventPublished published = eventCaptor.getValue();
        assertThat(published.request()).isSameAs(request);

        verifyNoMoreInteractions(publisher);
    }

    @Test
    void givenPublisherThrows_whenRecordIsInvoked_thenSwallowsExceptionAndDoesNotPropagate() {
        AuditEventRequest request = mock(AuditEventRequest.class);

        doThrow(new RuntimeException("ERROR")).when(publisher).publishEvent(any(AuditEventPublished.class));

        assertThatCode(() -> recorder.record(request)).doesNotThrowAnyException();

        verify(publisher).publishEvent(any(AuditEventPublished.class));
        verifyNoMoreInteractions(publisher);
    }
}
