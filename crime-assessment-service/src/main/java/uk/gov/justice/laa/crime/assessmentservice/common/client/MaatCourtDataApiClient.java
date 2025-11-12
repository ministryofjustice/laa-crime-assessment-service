package uk.gov.justice.laa.crime.assessmentservice.common.client;

import uk.gov.justice.laa.crime.assessmentservice.common.dto.IojAppealDTO;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange()
public interface MaatCourtDataApiClient {
    @GetExchange("/api/internal/v1/assessment/ioj-appeal/{legacyAppealId}")
    IojAppealDTO getIojAppeal(@PathVariable Integer legacyAppealId);
}
