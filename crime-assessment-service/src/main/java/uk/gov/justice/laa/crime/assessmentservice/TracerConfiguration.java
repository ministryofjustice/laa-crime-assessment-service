package uk.gov.justice.laa.crime.assessmentservice;

import io.micrometer.tracing.Tracer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@AllArgsConstructor
public class TracerConfiguration {

    public final Tracer tracer;

    @Bean
    TraceIdHandler traceIdHandler() {
        return new TraceIdHandler(tracer);
    }
}
