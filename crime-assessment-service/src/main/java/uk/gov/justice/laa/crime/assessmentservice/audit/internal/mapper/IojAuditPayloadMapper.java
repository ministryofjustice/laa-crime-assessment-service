package uk.gov.justice.laa.crime.assessmentservice.audit.internal.mapper;

import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppealMetadata;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@UtilityClass
public final class IojAuditPayloadMapper {

    public static Map<String, Object> mapFindDetails(UUID appealId) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("requestedId", appealId.toString());
        return details;
    }

    public static Map<String, Object> mapCreateDetails(ApiCreateIojAppealRequest request) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("iojAppeal", mapIojAppeal(request.getIojAppeal()));
        details.put("metadata", mapMetadata(request.getIojAppealMetadata()));

        return details;
    }

    private static Map<String, Object> mapIojAppeal(IojAppeal iojAppeal) {
        if (iojAppeal == null) return Map.of();

        Map<String, Object> m = new LinkedHashMap<>();
        putDate(m, "receivedDate", iojAppeal.getReceivedDate());
        putEnumValue(m, "appealReason", iojAppeal.getAppealReason());
        putEnumValue(m, "appealAssessor", iojAppeal.getAppealAssessor());
        putValue(m, "appealSuccessful", iojAppeal.getAppealSuccessful());
        putEnumValue(m, "decisionReason", iojAppeal.getDecisionReason());
        putDate(m, "decisionDate", iojAppeal.getDecisionDate());
        return m;
    }

    private static Map<String, Object> mapMetadata(IojAppealMetadata metadata) {
        if (metadata == null) return Map.of();

        Map<String, Object> m = new LinkedHashMap<>();
        putValue(m, "legacyApplicationId", metadata.getLegacyApplicationId());
        putDate(m, "applicationReceivedDate", metadata.getApplicationReceivedDate());
        putValue(m, "caseManagementUnitId", metadata.getCaseManagementUnitId());
        return m;
    }

    private static void putDate(Map<String, Object> map, String key, LocalDate date) {
        if (date != null) map.put(key, date.toString());
    }

    private static void putEnumValue(Map<String, Object> map, String key, Object enumValue) {
        if (enumValue != null) map.put(key, String.valueOf(enumValue));
    }

    private static void putValue(Map<String, Object> map, String key, Object value) {
        if (value != null) map.put(key, value);
    }
}
