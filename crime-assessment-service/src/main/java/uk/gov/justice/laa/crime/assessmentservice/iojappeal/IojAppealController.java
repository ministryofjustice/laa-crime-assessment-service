package uk.gov.justice.laa.crime.assessmentservice.iojappeal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.common.dto.IojAppealDTO;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.IojAppealService;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.service.LegacyIojAppealService;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.util.UUID;

import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/ioj-appeals")
@Tag(name = "IOJ Appeals", description = "Rest API for IOJ Appeals.")
public class IojAppealController {

    private final IojAppealService iojAppealService;

    private final LegacyIojAppealService legacyIojAppealService;

    @GetMapping(path = "/{appealId}")
    @Operation(description = "Find an IoJ Appeal")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404")
    @ApiResponse(responseCode = "500")
    public ResponseEntity<ApiGetIojAppealResponse> getAppeal(@PathVariable UUID appealId) {
        ApiGetIojAppealResponse response = iojAppealService.findIojAppeal(appealId);

        return response != null
                ? ResponseEntity.ok(response)
                : ResponseEntity.notFound().build();
    }

    @GetMapping(path = "/lookup-by-legacy-id/{legacyAppealId}")
    @Operation(description = "Find an IoJ Appeal by its legacy appeal ID")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404")
    @ApiResponse(responseCode = "500")
    public ResponseEntity<ApiGetIojAppealResponse> getAppealByLegacyAppealId(@PathVariable UUID legacyAppealId) {
        ApiGetIojAppealResponse response = legacyIojAppealService.findIojAppeal(legacyAppealId);

        return response != null
            ? ResponseEntity.ok(response)
            : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(description = "Create a new IoJ Appeal")
    @ApiResponse(responseCode = "501")
    public ResponseEntity<IojAppealDTO> createAppeal(@RequestBody IojAppealDTO request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
