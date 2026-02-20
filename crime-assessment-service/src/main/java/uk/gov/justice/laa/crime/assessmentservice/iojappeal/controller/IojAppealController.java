package uk.gov.justice.laa.crime.assessmentservice.iojappeal.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.common.api.exception.CrimeValidationException;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.dto.ApiRollbackIojAppealRequest;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.dto.ApiRollbackIojAppealResponse;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.IojAppealOrchestrationService;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator.ApiCreateIojAppealRequestValidator;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;
import uk.gov.justice.laa.crime.error.ErrorMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    private final IojAppealOrchestrationService iojAppealOrchestrationService;

    @GetMapping(path = "/{appealId}")
    public ResponseEntity<ApiGetIojAppealResponse> find(@PathVariable UUID appealId) {
        Optional<ApiGetIojAppealResponse> response = iojAppealOrchestrationService.find(appealId);

        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/lookup-by-legacy-id/{legacyAppealId}")
    public ResponseEntity<ApiGetIojAppealResponse> findByLegacyId(@PathVariable int legacyAppealId) {
        Optional<ApiGetIojAppealResponse> response = iojAppealOrchestrationService.find(legacyAppealId);

        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiCreateIojAppealResponse> create(@RequestBody ApiCreateIojAppealRequest request) {
        List<ErrorMessage> validationErrors = ApiCreateIojAppealRequestValidator.validateRequest(request);
        if (!validationErrors.isEmpty()) {
            throw new CrimeValidationException(validationErrors);
        }

        ApiCreateIojAppealResponse iojAppeal = iojAppealOrchestrationService.createIojAppeal(request);

        return ResponseEntity.ok(iojAppeal);
    }

    @PatchMapping(path = "/rollback/{appealId}")
    @Override
    public ResponseEntity<ApiRollbackIojAppealResponse> rollback(@PathVariable UUID appealId) {
        Optional<ApiGetIojAppealResponse> iojAppealResponse = iojAppealOrchestrationService.find(appealId);

        if (iojAppealResponse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ApiGetIojAppealResponse iojAppeal = iojAppealResponse.get();

        ApiRollbackIojAppealRequest rollbackRequest =
                new ApiRollbackIojAppealRequest(iojAppeal.getAppealId(), iojAppeal.getLegacyAppealId());
        boolean rollbackSuccessful = iojAppealOrchestrationService.rollbackIojAppeal(rollbackRequest);

        return ResponseEntity.ok(ApiRollbackIojAppealResponse.builder()
                .appealId(iojAppeal.getAppealId())
                .legacyAppealId(iojAppeal.getLegacyAppealId())
                .rollbackSuccessful(rollbackSuccessful)
                .build());
    }
}
