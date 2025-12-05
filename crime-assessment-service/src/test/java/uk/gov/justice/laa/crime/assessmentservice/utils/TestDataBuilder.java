package uk.gov.justice.laa.crime.assessmentservice.utils;

import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.common.model.common.ApiUserSession;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppealMetadata;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.IojAppealDecisionReason;
import uk.gov.justice.laa.crime.enums.NewWorkReason;

import java.time.LocalDate;
import java.util.UUID;

@UtilityClass
public class TestDataBuilder {

    public ApiCreateIojAppealRequest buildValidPopulatedCreateIojAppealRequest() {
        ApiCreateIojAppealRequest request = new ApiCreateIojAppealRequest();
        var appeal = new IojAppeal();
        appeal.setAppealSuccessful(true);
        appeal.setAppealReason(NewWorkReason.JR);
        appeal.setAppealAssessor(IojAppealAssessor.JUDGE);
        appeal.setDecisionReason(IojAppealDecisionReason.INTERESTS_PERSON);
        appeal.setReceivedDate(LocalDate.now());
        appeal.setDecisionDate(LocalDate.now());
        appeal.setNotes("Notes are Here");

        var metaData = new IojAppealMetadata();
        metaData.setApplicationReceivedDate(LocalDate.now().minusDays(7));
        metaData.setLegacyApplicationId(456);
        var session = new ApiUserSession();
        session.setUserName("Test User");
        session.setSessionId("Test Session");
        metaData.setUserSession(session);
        metaData.setCaseManagementUnitId(789);

        request.setIojAppeal(appeal);
        request.setIojAppealMetadata(metaData);
        return request;
    }

    public IojAppealEntity buildIojAppealEntity(boolean setRandomId) {
        return IojAppealEntity.builder()
                .legacyAppealId(1234)
                .legacyApplicationId(223)
                .receivedDate(LocalDate.of(2025, 2, 1))
                .appealReason(NewWorkReason.NEW.getCode())
                .appealAssessor(IojAppealAssessor.CASEWORKER.name())
                .appealSuccessful(true)
                .decisionReason("DAMAGE_TO_REPUTATION")
                .notes("Passing IoJ Appeal")
                .decisionDate(LocalDate.of(2025, 2, 8))
                .caseManagementUnitId(44)
                .createdBy("tester")
                .appealId((setRandomId ? UUID.randomUUID() : null))
                .build();
    }

    public IojAppealEntity buildIojAppealEntity() {
        return buildIojAppealEntity(false);
    }
}
