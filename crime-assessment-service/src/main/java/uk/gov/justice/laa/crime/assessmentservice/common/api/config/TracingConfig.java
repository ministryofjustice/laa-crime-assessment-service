package uk.gov.justice.laa.crime.assessmentservice.common.api.config;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import uk.gov.justice.laa.crime.tracing.TraceIdHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TracingConfig {

    private final Tracer tracer;

    @Bean
    public TraceIdHandler traceIdHandler() {
        return new TraceIdHandler(tracer);
    }
}
