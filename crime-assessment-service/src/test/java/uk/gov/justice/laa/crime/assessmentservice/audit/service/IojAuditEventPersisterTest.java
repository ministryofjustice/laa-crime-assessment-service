package uk.gov.justice.laa.crime.assessmentservice.audit.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditDomain;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventRequest;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditEventType;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditIdentifier;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditIdentifierType;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.entity.IojAuditEventEntity;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.repository.IojAuditEventRepository;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.service.IojAuditEventPersister;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class IojAuditEventPersisterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private IojAuditEventRepository repository;

    @InjectMocks
    private IojAuditEventPersister persister;

    @Captor
    private ArgumentCaptor<IojAuditEventEntity> entityCaptor;

    @Test
    void givenAllFieldsPresent_whenPersist_thenEntityIsBuiltAndSavedCorrectly() {
        UUID appealId = UUID.randomUUID();
        Long legacyAppealId = 123L;

        Object payload = new Object();
        JsonNode jsonPayload = mock(JsonNode.class);

        AuditIdentifier appealIdentifier = new AuditIdentifier(AuditIdentifierType.APPEAL_ID, appealId.toString());
        AuditIdentifier legacyIdentifier =
                new AuditIdentifier(AuditIdentifierType.LEGACY_APPEAL_ID, legacyAppealId.toString());

        AuditEventRequest request = AuditEventRequest.builder()
                .domain(AuditDomain.IOJ_APPEAL)
                .eventType(AuditEventType.CREATE)
                .triggeredBy("client-id")
                .traceId("trace-123")
                .auditPayload(payload)
                .identifiers(List.of(appealIdentifier, legacyIdentifier))
                .build();

        when(objectMapper.valueToTree(payload)).thenReturn(jsonPayload);

        persister.persist(request);

        verify(repository).save(entityCaptor.capture());

        IojAuditEventEntity entity = entityCaptor.getValue();

        assertThat(entity.getAppealId()).isEqualTo(appealId);
        assertThat(entity.getLegacyAppealId()).isEqualTo(legacyAppealId);
        assertThat(entity.getEventType()).isEqualTo(AuditEventType.CREATE);
        assertThat(entity.getAuditPayload()).isSameAs(jsonPayload);
        assertThat(entity.getTriggeredBy()).isEqualTo("client-id");
        assertThat(entity.getTraceId()).isEqualTo("trace-123");

        verifyNoMoreInteractions(repository, objectMapper);
    }

    @Test
    void givenNoIdentifiersAndNoPayload_whenPersist_thenEntityFieldsAreNull() {
        AuditEventRequest request = AuditEventRequest.builder()
                .domain(AuditDomain.IOJ_APPEAL)
                .eventType(AuditEventType.FIND)
                .triggeredBy("system")
                .traceId(null)
                .auditPayload(null)
                .identifiers(List.of())
                .build();

        persister.persist(request);

        verify(repository).save(entityCaptor.capture());

        IojAuditEventEntity entity = entityCaptor.getValue();

        assertThat(entity.getAppealId()).isNull();
        assertThat(entity.getLegacyAppealId()).isNull();
        assertThat(entity.getAuditPayload()).isNull();
        assertThat(entity.getEventType()).isEqualTo(AuditEventType.FIND);
        assertThat(entity.getTriggeredBy()).isEqualTo("system");
        assertThat(entity.getTraceId()).isNull();

        verifyNoInteractions(objectMapper);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void givenPayloadNull_whenPersist_thenObjectMapperIsNotCalled() {
        AuditEventRequest request = mock(AuditEventRequest.class);

        when(request.getIdentifierByName(any())).thenReturn(Optional.empty());
        when(request.auditPayload()).thenReturn(null);
        when(request.eventType()).thenReturn(AuditEventType.FIND);

        persister.persist(request);

        verify(repository).save(any(IojAuditEventEntity.class));
        verifyNoInteractions(objectMapper);
    }
}
