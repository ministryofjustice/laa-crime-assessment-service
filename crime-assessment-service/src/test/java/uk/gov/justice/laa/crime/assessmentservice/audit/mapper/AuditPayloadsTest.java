package uk.gov.justice.laa.crime.assessmentservice.audit.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditOutcome;
import uk.gov.justice.laa.crime.assessmentservice.audit.api.AuditPath;
import uk.gov.justice.laa.crime.assessmentservice.audit.internal.mapper.AuditPayloads;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AuditPayloadsTest {

    @ParameterizedTest
    @MethodSource("payloadFactories")
    void givenOutcomeAndPathAndDetails_whenFactoryCalled_thenEnvelopeContainsOutcomePathAndDetails(
            PayloadFactory factory) {
        Map<String, Object> details = Map.of("k", "v");

        Map<String, Object> payload = factory.create(AuditOutcome.SUCCESS, AuditPath.LOCAL_HIT, details);

        assertThat(payload)
                .containsEntry("outcome", AuditOutcome.SUCCESS)
                .containsEntry("path", AuditPath.LOCAL_HIT)
                .containsEntry("details", details)
                .isInstanceOf(LinkedHashMap.class)
                .containsOnlyKeys("outcome", "path", "details");
    }

    @ParameterizedTest
    @MethodSource("payloadFactories")
    void givenNullDetails_whenFactoryCalled_thenDetailsDefaultsToEmptyMap(PayloadFactory factory) {
        Map<String, Object> payload = factory.create(AuditOutcome.NOT_FOUND, AuditPath.LOCAL_MISS, null);

        assertThat(payload)
                .containsEntry("outcome", AuditOutcome.NOT_FOUND)
                .containsEntry("path", AuditPath.LOCAL_MISS);

        Object details = payload.get("details");
        assertThat(details).isInstanceOf(Map.class);
        assertThat((Map<?, ?>) details).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("findNoDetailsFactories")
    void givenOutcomeAndPath_whenFindPayloadCalledWithoutDetails_thenDetailsIsEmptyMap(NoDetailsFindFactory factory) {
        Map<String, Object> payload = factory.create(AuditOutcome.FAILURE, AuditPath.LOCAL_MISS_LEGACY_FAILURE);

        assertThat(payload)
                .containsEntry("outcome", AuditOutcome.FAILURE)
                .containsEntry("path", AuditPath.LOCAL_MISS_LEGACY_FAILURE);

        Object details = payload.get("details");
        assertThat(details).isInstanceOf(Map.class);
        assertThat((Map<?, ?>) details).isEmpty();
    }

    static Stream<Arguments> payloadFactories() {
        return Stream.of(Arguments.of((PayloadFactory) AuditPayloads::findPayload), Arguments.of((PayloadFactory)
                AuditPayloads::createPayload));
    }

    static Stream<Arguments> findNoDetailsFactories() {
        return Stream.of(Arguments.of((NoDetailsFindFactory) AuditPayloads::findPayload));
    }

    @FunctionalInterface
    interface PayloadFactory {

        Map<String, Object> create(AuditOutcome outcome, AuditPath path, Map<String, Object> details);
    }

    @FunctionalInterface
    interface NoDetailsFindFactory {

        Map<String, Object> create(AuditOutcome outcome, AuditPath path);
    }
}
