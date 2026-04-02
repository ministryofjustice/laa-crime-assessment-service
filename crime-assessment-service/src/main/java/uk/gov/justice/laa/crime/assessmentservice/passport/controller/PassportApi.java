package uk.gov.justice.laa.crime.assessmentservice.passport.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import uk.gov.justice.laa.crime.common.model.passported.ApiCreatePassportedAssessmentRequest;
import uk.gov.justice.laa.crime.common.model.passported.ApiCreatePassportedAssessmentResponse;
import uk.gov.justice.laa.crime.common.model.passported.ApiGetPassportedAssessmentResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface PassportApi {

    @Operation(description = "Find a Passport Assessment")
    @ApiResponse(
            responseCode = "200",
            description = "Passport Assessment found",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiGetPassportedAssessmentResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "404", description = "Passport Assessment not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<ApiGetPassportedAssessmentResponse> find(@PathVariable int legacyId);

    @Operation(description = "Create a Passport Assessment")
    @ApiResponse(
            responseCode = "200",
            description = "Passport Assessment created",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiCreatePassportedAssessmentResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad request")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<ApiCreatePassportedAssessmentResponse> create(
            @RequestBody ApiCreatePassportedAssessmentRequest request);
}
