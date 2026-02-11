package uk.gov.justice.laa.crime.assessmentservice.iojappeal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealRequest;
import uk.gov.justice.laa.crime.common.model.ioj.ApiCreateIojAppealResponse;
import uk.gov.justice.laa.crime.common.model.ioj.ApiGetIojAppealResponse;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface IojAppealApi {
    @Operation(description = "Find an IoJ Appeal")
    @ApiResponse(
            responseCode = "200",
            description = "IoJ Appeal found",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiGetIojAppealResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "IoJ Appeal not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<ApiGetIojAppealResponse> find(@PathVariable UUID appealId);

    @Operation(description = "Find an IoJ Appeal by its legacy appeal ID")
    @ApiResponse(
            responseCode = "200",
            description = "IoJ Appeal found",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiGetIojAppealResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "IoJ Appeal not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<ApiGetIojAppealResponse> findByLegacyId(@PathVariable int legacyAppealId);

    @Operation(description = "Create a new IoJ Appeal")
    @ApiResponse(
            responseCode = "200",
            description = "IoJ Appeal created successfully",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiCreateIojAppealResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<ApiCreateIojAppealResponse> create(@RequestBody ApiCreateIojAppealRequest request);
}
