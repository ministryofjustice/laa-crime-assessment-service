package uk.gov.justice.laa.crime.assessmentservice.iojappeal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRollbackIojAppealResponse {
    @NotNull
    private String appealId;

    private int legacyAppealId;

    private boolean rollbackSuccessful;
}
