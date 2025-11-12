package uk.gov.justice.laa.crime.assessmentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class ResourceServerConfiguration {

    private static final String SCOPE_CAS_STANDARD = "SCOPE_cas/standard";

    @Bean
    protected BearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint() {
        BearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint =
                new BearerTokenAuthenticationEntryPoint();
        bearerTokenAuthenticationEntryPoint.setRealmName("Crime Assessment Service API");
        return bearerTokenAuthenticationEntryPoint;
    }

    @Bean
    public AccessDeniedHandler bearerTokenAccessDeniedHandler() {
        return new BearerTokenAccessDeniedHandler();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(disableCsrfAsMadeRedundantByOath2AndJwt())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/open-api/**")
                        .permitAll()
                        .requestMatchers("/actuator/**")
                        .permitAll()
                        .requestMatchers("/swagger-ui/**")
                        .permitAll()
                        .requestMatchers("/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/api/**")
                        .hasAuthority(SCOPE_CAS_STANDARD)
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer((oauth2ResourceServer) -> oauth2ResourceServer
                        .accessDeniedHandler(bearerTokenAccessDeniedHandler())
                        .authenticationEntryPoint(bearerTokenAuthenticationEntryPoint())
                        .jwt(Customizer.withDefaults()));
        return http.build();
    }

    private Customizer<CsrfConfigurer<HttpSecurity>> disableCsrfAsMadeRedundantByOath2AndJwt() {
        return AbstractHttpConfigurer::disable;
    }
}
