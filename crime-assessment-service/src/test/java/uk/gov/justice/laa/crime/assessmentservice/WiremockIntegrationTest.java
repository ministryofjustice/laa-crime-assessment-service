package uk.gov.justice.laa.crime.assessmentservice;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import com.github.tomakehurst.wiremock.WireMockServer;

@EnableWireMock
@Import(CrimeAssessmentTestConfiguration.class)
@SpringBootTest(
        classes = AssessmentServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {"WIREMOCK_ISSUER_URI=http://localhost:${wiremock.server.port}"})
public abstract class WiremockIntegrationTest {

    @InjectWireMock
    protected static WireMockServer wiremock;
}
