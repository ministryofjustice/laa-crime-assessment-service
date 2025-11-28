package uk.gov.justice.laa.crime.assessmentservice.common.dto.maat;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreateIojAppealRequest {
    @NotNull
    private Integer repId;

    @NotNull
    private LocalDateTime appealSetupDate;

    @NotNull
    private String nworCode;

    @NotNull
    private Integer cmuId;

    @NotNull
    private String iapsStatus;

    private String appealSetupResult;
    private String iderCode;
    private LocalDateTime decisionDate;
    private String decisionResult;
    private String notes;

    @NotNull
    private String userCreated;
}
