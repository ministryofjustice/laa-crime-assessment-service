package uk.gov.justice.laa.crime.assessmentservice.audit.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.assessmentservice.audit.internal.mapper.IojAuditPayloadMapper;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppealMetadata;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.IojAppealDecisionReason;
import uk.gov.justice.laa.crime.enums.NewWorkReason;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IojAuditPayloadMapperTest {

    @Test
    void givenAppealId_whenCreateFindDetails_thenRequestedIdIsPopulatedAsString() {
        UUID appealId = UUID.randomUUID();

        Map<String, Object> details = IojAuditPayloadMapper.createFindDetails(appealId);

        assertThat(details).containsOnlyKeys("requestedId").containsEntry("requestedId", appealId.toString());
    }

    @Test
    void givenRequestWithNullIojAppealAndNullMetadata_whenCreateDetails_thenNestedMapsAreEmpty() {
        ApiCreateIojAppealRequest request = mock(ApiCreateIojAppealRequest.class);
        when(request.getIojAppeal()).thenReturn(null);
        when(request.getIojAppealMetadata()).thenReturn(null);

        Map<String, Object> details = IojAuditPayloadMapper.createDetails(request);

        assertThat(details)
                .containsOnlyKeys("iojAppeal", "metadata")
                .containsEntry("iojAppeal", Map.of())
                .containsEntry("metadata", Map.of());
    }

    @Test
    void givenRequestWithPopulatedIojAppealAndMetadata_whenCreateDetails_thenAllFieldsAreMapped() {
        ApiCreateIojAppealRequest request = mock(ApiCreateIojAppealRequest.class);

        IojAppeal iojAppeal = mock(IojAppeal.class);
        IojAppealMetadata metadata = mock(IojAppealMetadata.class);

        when(request.getIojAppeal()).thenReturn(iojAppeal);
        when(request.getIojAppealMetadata()).thenReturn(metadata);

        when(iojAppeal.getAppealSuccessful()).thenReturn(Boolean.TRUE);
        when(iojAppeal.getAppealReason()).thenReturn(NewWorkReason.NEW);
        when(iojAppeal.getAppealAssessor()).thenReturn(IojAppealAssessor.CASEWORKER);
        when(iojAppeal.getDecisionReason()).thenReturn(IojAppealDecisionReason.DAMAGE_TO_REPUTATION);
        when(iojAppeal.getReceivedDate()).thenReturn(LocalDate.of(2026, 2, 1));
        when(iojAppeal.getDecisionDate()).thenReturn(LocalDate.of(2026, 2, 2));

        when(metadata.getCaseManagementUnitId()).thenReturn(99);
        when(metadata.getLegacyApplicationId()).thenReturn(12345);
        when(metadata.getApplicationReceivedDate()).thenReturn(LocalDate.of(2026, 1, 31));

        Map<String, Object> details = IojAuditPayloadMapper.createDetails(request);

        assertThat(details).containsOnlyKeys("iojAppeal", "metadata");

        assertThat(details.get("iojAppeal")).isInstanceOf(Map.class);
        assertThat(details.get("metadata")).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> iojMap = (Map<String, Object>) details.get("iojAppeal");

        @SuppressWarnings("unchecked")
        Map<String, Object> metaMap = (Map<String, Object>) details.get("metadata");

        assertThat(iojMap)
                .containsEntry("receivedDate", "2026-02-01")
                .containsEntry("appealReason", NewWorkReason.NEW.getCode())
                .containsEntry("appealAssessor", IojAppealAssessor.CASEWORKER.toString())
                .containsEntry("appealSuccessful", true)
                .containsEntry("decisionReason", IojAppealDecisionReason.DAMAGE_TO_REPUTATION.toString())
                .containsEntry("decisionDate", "2026-02-02");

        assertThat(metaMap)
                .containsEntry("legacyApplicationId", 12345)
                .containsEntry("applicationReceivedDate", "2026-01-31")
                .containsEntry("caseManagementUnitId", 99);
    }

    @ParameterizedTest
    @MethodSource("iojAppealNullFieldCases")
    void givenIojAppealWithNullField_whenCreateDetails_thenNullFieldIsOmitted(
            LocalDate receivedDate,
            NewWorkReason appealReason,
            IojAppealAssessor appealAssessor,
            Boolean appealSuccessful,
            IojAppealDecisionReason decisionReason,
            LocalDate decisionDate,
            String expectedMissingKey) {
        ApiCreateIojAppealRequest request = mock(ApiCreateIojAppealRequest.class);
        IojAppeal iojAppeal = mock(IojAppeal.class);

        when(request.getIojAppeal()).thenReturn(iojAppeal);
        when(request.getIojAppealMetadata()).thenReturn(null);

        when(iojAppeal.getReceivedDate()).thenReturn(receivedDate);
        when(iojAppeal.getAppealReason()).thenReturn(appealReason);
        when(iojAppeal.getAppealAssessor()).thenReturn(appealAssessor);
        when(iojAppeal.getAppealSuccessful()).thenReturn(appealSuccessful);
        when(iojAppeal.getDecisionReason()).thenReturn(decisionReason);
        when(iojAppeal.getDecisionDate()).thenReturn(decisionDate);

        Map<String, Object> details = IojAuditPayloadMapper.createDetails(request);

        @SuppressWarnings("unchecked")
        Map<String, Object> iojMap = (Map<String, Object>) details.get("iojAppeal");

        assertThat(iojMap).doesNotContainKey(expectedMissingKey);
    }

    static Stream<Arguments> iojAppealNullFieldCases() {
        return Stream.of(
                Arguments.of(
                        null,
                        NewWorkReason.NEW,
                        IojAppealAssessor.CASEWORKER,
                        true,
                        IojAppealDecisionReason.LOSS_OF_LIBERTY,
                        LocalDate.of(2026, 2, 2),
                        "receivedDate"),
                Arguments.of(
                        LocalDate.of(2026, 2, 1),
                        null,
                        IojAppealAssessor.CASEWORKER,
                        true,
                        IojAppealDecisionReason.LOSS_OF_LIBERTY,
                        LocalDate.of(2026, 2, 2),
                        "appealReason"),
                Arguments.of(
                        LocalDate.of(2026, 2, 1),
                        NewWorkReason.NEW,
                        null,
                        true,
                        IojAppealDecisionReason.LOSS_OF_LIBERTY,
                        LocalDate.of(2026, 2, 2),
                        "appealAssessor"),
                Arguments.of(
                        LocalDate.of(2026, 2, 1),
                        NewWorkReason.NEW,
                        IojAppealAssessor.CASEWORKER,
                        null,
                        IojAppealDecisionReason.LOSS_OF_LIBERTY,
                        LocalDate.of(2026, 2, 2),
                        "appealSuccessful"),
                Arguments.of(
                        LocalDate.of(2026, 2, 1),
                        NewWorkReason.NEW,
                        IojAppealAssessor.CASEWORKER,
                        true,
                        null,
                        LocalDate.of(2026, 2, 2),
                        "decisionReason"),
                Arguments.of(
                        LocalDate.of(2026, 2, 1),
                        NewWorkReason.NEW,
                        IojAppealAssessor.CASEWORKER,
                        true,
                        IojAppealDecisionReason.LOSS_OF_LIBERTY,
                        null,
                        "decisionDate"));
    }

    @ParameterizedTest
    @MethodSource("metadataNullFieldCases")
    void givenMetadataWithNullField_whenCreateDetails_thenNullFieldIsOmitted(
            Integer legacyApplicationId,
            LocalDate applicationReceivedDate,
            Integer caseManagementUnitId,
            String expectedMissingKey) {
        ApiCreateIojAppealRequest request = mock(ApiCreateIojAppealRequest.class);
        IojAppealMetadata metadata = mock(IojAppealMetadata.class);

        when(request.getIojAppeal()).thenReturn(null);
        when(request.getIojAppealMetadata()).thenReturn(metadata);

        when(metadata.getLegacyApplicationId()).thenReturn(legacyApplicationId);
        when(metadata.getApplicationReceivedDate()).thenReturn(applicationReceivedDate);
        when(metadata.getCaseManagementUnitId()).thenReturn(caseManagementUnitId);

        Map<String, Object> details = IojAuditPayloadMapper.createDetails(request);

        @SuppressWarnings("unchecked")
        Map<String, Object> metaMap = (Map<String, Object>) details.get("metadata");

        assertThat(metaMap).doesNotContainKey(expectedMissingKey);
    }

    static Stream<Arguments> metadataNullFieldCases() {
        return Stream.of(
                Arguments.of(null, LocalDate.of(2026, 1, 31), 99, "legacyApplicationId"),
                Arguments.of(12345, null, 99, "applicationReceivedDate"),
                Arguments.of(12345, LocalDate.of(2026, 1, 31), null, "caseManagementUnitId"));
    }
}
