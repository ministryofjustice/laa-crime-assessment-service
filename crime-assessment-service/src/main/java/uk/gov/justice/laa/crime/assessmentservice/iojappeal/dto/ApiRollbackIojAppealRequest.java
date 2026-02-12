package uk.gov.justice.laa.crime.assessmentservice.iojappeal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRollbackIojAppealRequest {
    @NotNull
    private UUID appealId;

    private int legacyAppealId;

    public ApiRollbackIojAppealRequest(String appealId, int legacyAppealId) {
        this.appealId = UUID.fromString(appealId);
        this.legacyAppealId = legacyAppealId;
    }
}
