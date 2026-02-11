package uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ioj_appeal", schema = "ioj_appeal")
public class IojAppealEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appeal_id")
    private UUID appealId;

    @Column(name = "legacy_appeal_id")
    private Integer legacyAppealId;

    @Column(name = "legacy_application_id")
    private int legacyApplicationId;

    @Column(name = "receipt_date")
    private LocalDate receivedDate;

    @Column(name = "reason")
    private String appealReason;

    @Column(name = "assessor")
    private String appealAssessor;

    @Column(name = "is_passed")
    private boolean appealSuccessful;

    @Column(name = "decision_reason")
    private String decisionReason;

    @Column(name = "notes")
    private String notes;

    @Column(name = "decision_date")
    private LocalDate decisionDate;

    @Column(name = "case_management_unit_id")
    private int caseManagementUnitId;

    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdDate = LocalDateTime.now();
}
