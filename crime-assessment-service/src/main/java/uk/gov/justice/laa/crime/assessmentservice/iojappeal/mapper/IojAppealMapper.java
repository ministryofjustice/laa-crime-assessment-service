package uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper;

import uk.gov.justice.laa.crime.assessmentservice.common.dto.maat.CreateIojAppealRequest;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.IojAppealDecision;
import uk.gov.justice.laa.crime.enums.IojAppealDecisionReason;

import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        builder = @Builder(disableBuilder = true))
public abstract class IojAppealMapper {

    public static final String SETUP_RESULT_JUDGE = "REFER";
    public static final String SETUP_RESULT_CASEWORKER_PASS = "GRANT";
    public static final String SETUP_RESULT_CASEWORKER_FAIL = "REFUSED";
    public static final String ASSESSMENT_STATUS_COMPLETE = "COMPLETE";

    @Mapping(target = "appealSuccessful", source = "iojAppealEntity.isPassed")
    public abstract ApiGetIojAppealResponse mapEntityToDTO(IojAppealEntity iojAppealEntity);

    @Mapping(target = "isPassed", source = "iojAppeal.appealSuccessful")
    public abstract IojAppealEntity mapAppealToEntity(IojAppeal iojAppeal);

    public IojAppealEntity mapCreateAppealToEntity(ApiCreateIojAppealRequest request) {
        var appealEntity = mapAppealToEntity(request.getIojAppeal());
        appealEntity.setLegacyApplicationId(request.getIojAppealMetadata().getLegacyApplicationId());
        appealEntity.setCaseManagementUnitId(request.getIojAppealMetadata().getCaseManagementUnitId());
        return appealEntity;
    }

    public CreateIojAppealRequest mapEntityToCreateAppealRequest(IojAppealEntity entity) {
        return CreateIojAppealRequest.builder()
                .repId(entity.getLegacyApplicationId())
                .cmuId(entity.getCaseManagementUnitId())
                .appealSetupDate(entity.getReceivedDate().atStartOfDay())
                .appealSetupResult(getSetupResult(entity))
                .decisionDate(entity.getDecisionDate().atStartOfDay())
                .decisionResult(getDecisionResult(entity.getIsPassed()))
                .iapsStatus(ASSESSMENT_STATUS_COMPLETE)
                .iderCode(IojAppealDecisionReason.valueOf(entity.getDecisionReason())
                        .getCode())
                .nworCode(entity.getAppealReason())
                .notes(entity.getNotes())
                .userCreated("TODO") // TODO Whomst?
                .build();
    }

    private String getDecisionResult(boolean decision) {
        return (decision) ? IojAppealDecision.PASS.name() : IojAppealDecision.FAIL.name();
    }

    private String getSetupResult(IojAppealEntity entity) {
        if (IojAppealAssessor.JUDGE.name().equals(entity.getAppealAssessor())) {
            // we've a judge, so appeal Setup result can only be refer.
            return SETUP_RESULT_JUDGE;
        } else {
            if (Boolean.TRUE.equals(entity.getIsPassed())) {
                return SETUP_RESULT_CASEWORKER_PASS;
            } else {
                return SETUP_RESULT_CASEWORKER_FAIL;
            }
        }
    }
}
