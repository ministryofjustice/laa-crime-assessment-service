package uk.gov.justice.laa.crime.assessmentservice.iojappeal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iojappeal")
public class IojAppealController {

    @GetMapping(path = "/{id}")
    public Object get(@PathVariable int id) throws Exception {
        throw new Exception("Not implemented");
    }
}
