package uk.gov.justice.laa.crime.assessmentservice.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IojAppealDTO {
    private Integer appealId;
}
