package uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;

import org.junit.jupiter.api.Test;

class IoJAppealMapperTest {

    private final IojAppealMapper iojAppealMapper = new IojAppealMapperImpl();

    @Test
    void whenCreateRequest_whenMapCreateAppealToEntity_thenAllFieldsMapped() {
        var request = TestDataBuilder.buildValidPopulatedCreateIojAppealRequest();
        var entity = iojAppealMapper.mapCreateAppealRequestToEntity(request);
        // check only nulls are known, non-mapped fields.
        assertThat(entity)
                .isNotNull()
                .hasNoNullFieldsOrPropertiesExcept(
                        "appealId", "legacyAppealId", "modifiedBy", "modifiedDate");
        assertThat(entity.getCreatedBy())
                .isEqualTo(request.getIojAppealMetadata().getUserSession().getUserName());
    }
}
