package uk.gov.justice.laa.crime.assessmentservice.utils;

import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.common.model.common.ApiUserSession;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppeal;
import uk.gov.justice.laa.crime.common.model.ioj.IojAppealMetadata;
import uk.gov.justice.laa.crime.common.model.passported.ApiCreatePassportedAssessmentRequest;
import uk.gov.justice.laa.crime.common.model.passported.ApiCreatePassportedAssessmentResponse;
import uk.gov.justice.laa.crime.common.model.passported.DeclaredBenefit;
import uk.gov.justice.laa.crime.common.model.passported.PassportedAssessment;
import uk.gov.justice.laa.crime.common.model.passported.PassportedAssessmentMetadata;
import uk.gov.justice.laa.crime.enums.BenefitRecipient;
import uk.gov.justice.laa.crime.enums.BenefitType;
import uk.gov.justice.laa.crime.enums.IojAppealAssessor;
import uk.gov.justice.laa.crime.enums.IojAppealDecisionReason;
import uk.gov.justice.laa.crime.enums.NewWorkReason;
import uk.gov.justice.laa.crime.enums.PassportAssessmentDecision;
import uk.gov.justice.laa.crime.enums.PassportAssessmentDecisionReason;
import uk.gov.justice.laa.crime.enums.ReviewType;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        metaData.setCaseManagementUnitId(789);
        metaData.setUserSession(buildPopulatedUserSession());

        request.setIojAppeal(appeal);
        request.setIojAppealMetadata(metaData);
        return request;
    }

    public ApiUserSession buildPopulatedUserSession() {
        var session = new ApiUserSession();
        session.setUserName("Test User");
        session.setSessionId("Test Session");
        return session;
    }

    public ApiCreateIojAppealResponse buildValidPopulatedCreateIojAppealResponse() {
        return new ApiCreateIojAppealResponse()
                .withAppealId(TestConstants.APPEAL_ID)
                .withLegacyAppealId(TestConstants.LEGACY_APPEAL_ID);
    }

    public IojAppealEntity buildIojAppealEntity(boolean setAppealId) {
        return IojAppealEntity.builder()
                .legacyAppealId(TestConstants.LEGACY_APPEAL_ID)
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
                .appealId((setAppealId ? UUID.fromString(TestConstants.APPEAL_ID) : null))
                .build();
    }

    public IojAppealEntity buildIojAppealEntity() {
        return buildIojAppealEntity(false);
    }

    public DeclaredBenefit buildDeclaredBenefit() {
        return new DeclaredBenefit()
                .withBenefitType(BenefitType.ESA)
                .withBenefitRecipient(BenefitRecipient.APPLICANT)
                .withLastSignOnDate(TestConstants.TEST_DATE)
                .withLegacyPartnerId(456);
    }

    public ApiCreatePassportedAssessmentRequest buildValidPopulatedCreatePassportedAssessmentRequest() {
        ApiCreatePassportedAssessmentRequest request = new ApiCreatePassportedAssessmentRequest();

        PassportedAssessment pa = new PassportedAssessment()
                .withAssessmentDate(LocalDateTime.now())
                .withNotes("Test Notes")
                .withDecisionReason(PassportAssessmentDecisionReason.DOCUMENTATION_SUPPLIED)
                .withAssessmentDecision(PassportAssessmentDecision.PASS)
                .withAssessmentReason(NewWorkReason.NEW)
                .withDeclaredUnder18(true)
                .withReviewType(ReviewType.NAFI)
                .withDeclaredBenefit(buildDeclaredBenefit());

        request.setPassportedAssessment(pa);

        PassportedAssessmentMetadata pam = new PassportedAssessmentMetadata()
                .withApplicationId(123)
                .withLegacyApplicationId(456)
                .withUserSession(buildPopulatedUserSession())
                .withCaseManagementUnitId(1000)
                .withUsn(22200);

        request.setPassportedAssessmentMetadata(pam);

        return request;
    }

    public ApiCreatePassportedAssessmentResponse buildValidPopulatedCreatePassportedAssessmentResponse() {
        return new ApiCreatePassportedAssessmentResponse()
                .withAssessmentId(TestConstants.APPEAL_ID)
                .withLegacyAssessmentId(TestConstants.LEGACY_APPEAL_ID);
    }
}
