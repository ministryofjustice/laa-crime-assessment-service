package uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper;

import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;

import org.mapstruct.Builder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        builder = @Builder(disableBuilder = true))
public abstract class IojAppealMapper {

    public abstract ApiGetIojAppealResponse mapEntityToDTO(IojAppealEntity iojAppealEntity);

    public abstract IojAppealEntity mapAppealToEntity(IojAppeal iojAppeal);

    public IojAppealEntity mapCreateAppealRequestToEntity(ApiCreateIojAppealRequest request) {
        IojAppealEntity appealEntity = mapAppealToEntity(request.getIojAppeal());
        appealEntity.setLegacyApplicationId(request.getIojAppealMetadata().getLegacyApplicationId());
        appealEntity.setCaseManagementUnitId(request.getIojAppealMetadata().getCaseManagementUnitId());
        appealEntity.setCreatedBy(
                request.getIojAppealMetadata().getUserSession().getUserName());
        return appealEntity;
    }
}
