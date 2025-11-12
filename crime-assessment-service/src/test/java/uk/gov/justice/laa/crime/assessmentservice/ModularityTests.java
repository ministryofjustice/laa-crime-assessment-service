package uk.gov.justice.laa.crime.assessmentservice;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import com.tngtech.archunit.core.domain.JavaClass;

class ModularityTests {
    ApplicationModules modules = ApplicationModules.of(
                    AssessmentServiceApplication.class,
                    JavaClass.Predicates.resideInAnyPackage("uk.gov.justice.laa.crime.assessmentservice.common.."))
            .verify();

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
