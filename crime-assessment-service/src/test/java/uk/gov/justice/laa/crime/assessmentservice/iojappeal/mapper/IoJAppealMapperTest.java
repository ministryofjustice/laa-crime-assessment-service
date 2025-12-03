package uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper.IojAppealMapper.SETUP_RESULT_CASEWORKER_FAIL;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper.IojAppealMapper.SETUP_RESULT_CASEWORKER_PASS;
import static uk.gov.justice.laa.crime.assessmentservice.iojappeal.mapper.IojAppealMapper.SETUP_RESULT_JUDGE;

import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.utils.TestDataBuilder;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.IojAppealDecision;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IoJAppealMapperTest {

    private final IojAppealMapper iojAppealMapper = new IojAppealMapperImpl();

    @Test
    void givenAppealEntity_whenMapEntityToCreateRequest_thenRequestIsCorrectlyMapped() {
        IojAppealEntity entity = TestDataBuilder.buildIojAppealEntity(true);
        // with full entity built, all should be mapped.
        assertThat(iojAppealMapper.mapEntityToCreateAppealRequest(entity)).isNotNull().hasNoNullFieldsOrProperties();
    }

    // Appeal Reason/Assessor Combination Tests
    private static Stream<Arguments> assessorIsPassedCombinations() {
        return Stream.of(
                Arguments.of(IojAppealAssessor.CASEWORKER, true, SETUP_RESULT_CASEWORKER_PASS),
                Arguments.of(IojAppealAssessor.CASEWORKER, false, SETUP_RESULT_CASEWORKER_FAIL),
                Arguments.of(IojAppealAssessor.JUDGE, true, SETUP_RESULT_JUDGE),
                Arguments.of(IojAppealAssessor.JUDGE, false, SETUP_RESULT_JUDGE));
    }

    @ParameterizedTest
    @MethodSource("assessorIsPassedCombinations")
    void givenAssessorAndIsPassed_whenMapIsCalled_thenCorrectSetupResultFound(
            IojAppealAssessor assessor, boolean isPassed, String expectedSetupReason) {
        IojAppealEntity entity = TestDataBuilder.buildIojAppealEntity(true);
        entity.setAppealAssessor(assessor.name());
        entity.setIsPassed(isPassed);
        var request = iojAppealMapper.mapEntityToCreateAppealRequest(entity);
        assertThat(request.getAppealSetupResult()).isEqualTo(expectedSetupReason);
        assertThat(request).hasNoNullFieldsOrProperties();
        if (isPassed) {
            assertThat(request.getDecisionResult()).isEqualTo(IojAppealDecision.PASS.name());
        } else {
            assertThat(request.getDecisionResult()).isEqualTo(IojAppealDecision.FAIL.name());
        }
    }
}
