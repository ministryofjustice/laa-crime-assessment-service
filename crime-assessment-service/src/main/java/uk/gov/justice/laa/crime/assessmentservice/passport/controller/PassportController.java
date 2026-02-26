package uk.gov.justice.laa.crime.assessmentservice.passport.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.passport.service.PassportService;
import uk.gov.justice.laa.crime.common.model.passported.ApiGetPassportedAssessmentResponse;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/v1/passport")
@Tag(name = "Passport", description = "Rest API for Passporting.")
public class PassportController implements PassportApi {
    private final PassportService passportService;

    @GetMapping(path = "/lookup-by-legacy-id/{legacyId}")
    public ResponseEntity<ApiGetPassportedAssessmentResponse> find(@PathVariable int legacyId) {
        Optional<ApiGetPassportedAssessmentResponse> response = passportService.find(legacyId);

        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
