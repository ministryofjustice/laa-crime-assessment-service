package uk.gov.justice.laa.crime.assessmentservice.passport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/passport")
public class PassportController {

    @GetMapping(path = "/{id}")
    public Object get(@PathVariable int id) throws Exception {
        throw new Exception("Not implemented");
    }
}
