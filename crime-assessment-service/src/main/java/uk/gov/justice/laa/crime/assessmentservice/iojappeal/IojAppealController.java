package uk.gov.justice.laa.crime.assessmentservice.iojappeal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.iojappeal.validator.ApiCreateIojAppealRequestValidator;

import java.util.List;

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
@RequestMapping("/api/v1/iojappeal")
@Tag(name = "IOJ Appeals", description = "Rest API for IOJ Appeals.")
public class IojAppealController {

    @GetMapping(path = "/{id}")
    @Operation(description = "Retrieve an IoJ Appeal")
    @ApiResponse(responseCode = "501")
    public ResponseEntity<ApiGetIojAppealResponse> get(@PathVariable int id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping(path = "/")
    @Operation(description = "Create a new IoJ Appeal record")
    @ApiResponse(responseCode = "501")
    public ResponseEntity<ApiCreateIojAppealResponse> create(@RequestBody ApiCreateIojAppealRequest request) {
        request.setIojAppeal(new IojAppeal());
        request.setIojAppealMetadata(new IojAppealMetadata());
        List<String> validationErrors = ApiCreateIojAppealRequestValidator.validateRequest(request);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping(path = "/lookup-by-legacy-id/{id}")
    @Operation(description = "Retrieve an IoJ Appeal by its legacy MAAT Appeal ID")
    @ApiResponse(responseCode = "501")
    public ResponseEntity<ApiGetIojAppealResponse> getByLegacyId(@PathVariable int id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
