package uk.gov.justice.laa.crime.assessmentservice.audit.internal.helper;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TriggeredByResolver {

    private static final String FALLBACK = "anonymous";

    public String resolve() {
        return resolveOptional().orElse(FALLBACK);
    }

    public Optional<String> resolveOptional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof JwtAuthenticationToken jwtAuth) || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        return Optional.ofNullable(jwtAuth.getToken().getClaimAsString("client_id"))
                .filter(s -> !s.isBlank());
    }
}
