package uk.gov.justice.laa.crime.assessmentservice.iojappeal.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.common.exception.CrimeValidationException;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.IojAppealDualWriteService;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.IojAppealService;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.LegacyIojAppealService;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator.ApiCreateIojAppealRequestValidator;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/v1/ioj-appeals")
@Tag(name = "IOJ Appeals", description = "Rest API for IOJ Appeals.")
public class IojAppealController implements IojAppealApi {

    private final IojAppealService iojAppealService;
    private final IojAppealDualWriteService iojAppealDualWriteService;
    private final LegacyIojAppealService legacyIojAppealService;

    @GetMapping(path = "/{appealId}")
    public ResponseEntity<ApiGetIojAppealResponse> getAppeal(@PathVariable UUID appealId) {
        Optional<ApiGetIojAppealResponse> response = iojAppealService.find(appealId);

        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/lookup-by-legacy-id/{legacyAppealId}")
    public ResponseEntity<ApiGetIojAppealResponse> getAppealByLegacyAppealId(@PathVariable int legacyAppealId) {
        ApiGetIojAppealResponse response = legacyIojAppealService.find(legacyAppealId);

        return response != null
                ? ResponseEntity.ok(response)
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ApiCreateIojAppealResponse> create(@RequestBody ApiCreateIojAppealRequest request) {
        List<String> validationErrors = ApiCreateIojAppealRequestValidator.validateRequest(request);
        if (!validationErrors.isEmpty()) {
            throw new CrimeValidationException(validationErrors);
        }
        // Call dual-write method.
        IojAppealEntity appealEntity = iojAppealDualWriteService.createIojAppeal(request);

        ApiCreateIojAppealResponse response = new ApiCreateIojAppealResponse()
                .withAppealId(appealEntity.getAppealId().toString())
                .withLegacyAppealId(appealEntity.getLegacyAppealId());
        return ResponseEntity.ok(response);
    }
}
