package uk.gov.justice.laa.crime.assessmentservice.common.client;

import org.springframework.web.service.annotation.PatchExchange;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange()
public interface MaatCourtDataApiClient {
    @GetExchange("/api/internal/v2/assessment/ioj-appeals/{legacyAppealId}")
    ApiGetIojAppealResponse getIojAppeal(@PathVariable Integer legacyAppealId);

    @PostExchange("/api/internal/v2/assessment/ioj-appeals")
    ApiCreateIojAppealResponse createIojAppeal(@RequestBody ApiCreateIojAppealRequest request);

    @PatchExchange("/api/internal/v2/assessment/ioj-appeals/rollback/{legacyAppealId}")
    void rollbackIojAppeal(@PathVariable Integer legacyAppealId);
}
