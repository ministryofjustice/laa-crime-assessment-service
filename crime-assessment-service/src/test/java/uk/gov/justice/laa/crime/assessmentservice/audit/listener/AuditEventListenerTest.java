package uk.gov.justice.laa.crime.assessmentservice.audit.listener;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditDomain;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventRequest;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.listener.AuditEventListener;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.listener.AuditEventPublished;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.service.IojAuditEventPersister;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditEventListenerTest {

    @Mock
    private IojAuditEventPersister iojPersister;

    @InjectMocks
    private AuditEventListener listener;

    @Test
    void givenIojAppealDomain_whenOn_thenPersists() {
        AuditEventRequest request = mock(AuditEventRequest.class);
        when(request.domain()).thenReturn(AuditDomain.IOJ_APPEAL);

        AuditEventPublished event = mock(AuditEventPublished.class);
        when(event.request()).thenReturn(request);

        listener.on(event);

        verify(iojPersister).persist(request);
        verifyNoMoreInteractions(iojPersister);
    }

    @Test
    void givenUnsupportedDomain_whenOn_thenDoesNotPersist() {
        AuditEventRequest request = mock(AuditEventRequest.class);
        when(request.domain()).thenReturn(AuditDomain.PASSPORT);

        AuditEventPublished event = mock(AuditEventPublished.class);
        when(event.request()).thenReturn(request);

        listener.on(event);

        verifyNoInteractions(iojPersister);
    }

    @Test
    void givenPersisterThrows_whenOn_thenSwallowsExceptionAndDoesNotPropagate() {
        AuditEventRequest request = mock(AuditEventRequest.class);
        when(request.domain()).thenReturn(AuditDomain.IOJ_APPEAL);

        AuditEventPublished event = mock(AuditEventPublished.class);
        when(event.request()).thenReturn(request);

        doThrow(new RuntimeException("db down")).when(iojPersister).persist(request);

        // should not throw
        listener.on(event);

        verify(iojPersister).persist(request);
        verifyNoMoreInteractions(iojPersister);
    }
}
