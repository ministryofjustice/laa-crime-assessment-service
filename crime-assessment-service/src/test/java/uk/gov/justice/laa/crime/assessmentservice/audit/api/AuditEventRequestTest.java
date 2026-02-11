package uk.gov.justice.laa.crime.assessmentservice.audit.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class AuditEventRequestTest {

    @Test
    void givenNullIdentifiers_whenGetIdentifierByName_thenEmptyIsReturned() {
        AuditEventRequest request = AuditEventRequest.builder()
                .domain(AuditDomain.IOJ_APPEAL)
                .identifiers(null)
                .eventType(AuditEventType.FIND)
                .auditPayload(null)
                .triggeredBy("client")
                .traceId(null)
                .build();

        Optional<AuditIdentifier> result = request.getIdentifierByName(AuditIdentifierType.APPEAL_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void givenEmptyIdentifiers_whenGetIdentifierByName_thenEmptyIsReturned() {
        AuditEventRequest request = AuditEventRequest.builder()
                .domain(AuditDomain.IOJ_APPEAL)
                .identifiers(List.of())
                .eventType(AuditEventType.FIND)
                .auditPayload(null)
                .triggeredBy("client")
                .traceId(null)
                .build();

        Optional<AuditIdentifier> result = request.getIdentifierByName(AuditIdentifierType.APPEAL_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void givenIdentifiersWithoutRequestedType_whenGetIdentifierByName_thenEmptyIsReturned() {
        AuditIdentifier legacyId = new AuditIdentifier(AuditIdentifierType.LEGACY_APPEAL_ID, "123");

        AuditEventRequest request = AuditEventRequest.builder()
                .domain(AuditDomain.IOJ_APPEAL)
                .identifiers(List.of(legacyId))
                .eventType(AuditEventType.FIND)
                .auditPayload(null)
                .triggeredBy("client")
                .traceId(null)
                .build();

        Optional<AuditIdentifier> result = request.getIdentifierByName(AuditIdentifierType.APPEAL_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void givenIdentifiersContainingRequestedType_whenGetIdentifierByName_thenFirstMatchIsReturned() {
        AuditIdentifier first = new AuditIdentifier(AuditIdentifierType.APPEAL_ID, "a1");
        AuditIdentifier second = new AuditIdentifier(AuditIdentifierType.APPEAL_ID, "a2");

        AuditEventRequest request = AuditEventRequest.builder()
                .domain(AuditDomain.IOJ_APPEAL)
                .identifiers(List.of(first, second))
                .eventType(AuditEventType.CREATE)
                .auditPayload(new Object())
                .triggeredBy("client")
                .traceId("trace-123")
                .build();

        Optional<AuditIdentifier> result = request.getIdentifierByName(AuditIdentifierType.APPEAL_ID);

        assertThat(result).contains(first);
    }
}
