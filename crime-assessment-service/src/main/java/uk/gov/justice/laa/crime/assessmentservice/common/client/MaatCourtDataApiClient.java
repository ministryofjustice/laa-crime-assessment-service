package uk.gov.justice.laa.crime.assessmentservice.common.client;

import uk.gov.justice.laa.crime.assessmentservice.common.dto.maat.CreateIojAppealRequest;
import uk.gov.justice.laa.crime.assessmentservice.common.dto.maat.IojAppealDTO;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange()
public interface MaatCourtDataApiClient {
    @GetExchange("/api/internal/v1/assessment/ioj-appeal/{legacyAppealId}")
    ApiGetIojAppealResponse getIojAppeal(@PathVariable Integer legacyAppealId);

    @PostExchange("/api/internal/v1/assessment/ioj-appeal")
    IojAppealDTO createIojAppeal(@RequestBody CreateIojAppealRequest request);
}
