package uk.gov.justice.laa.crime.assessmentservice.audit.internal.helper;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientIdResolver {

    private static final String FALLBACK = "anonymous";
    private static final String CLAIM_CLIENT_ID = "client_id";

    public String resolveOrAnonymous() {
        return resolveOptional().orElse(FALLBACK);
    }

    public Optional<String> resolveOptional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            log.warn("AUDIT clientId fallback used: no Authentication in SecurityContext");
            return Optional.empty();
        }

        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            log.warn(
                    "AUDIT clientId fallback used: non-JWT Authentication (authType={})",
                    auth.getClass().getSimpleName());
            return Optional.empty();
        }

        if (!auth.isAuthenticated()) {
            log.warn("AUDIT clientId fallback used: unauthenticated JWT token");
            return Optional.empty();
        }

        String clientId = jwtAuth.getToken().getClaimAsString(CLAIM_CLIENT_ID);

        if (clientId == null || clientId.isBlank()) {
            log.warn("AUDIT clientId fallback used: missing or blank '{}' claim in JWT", CLAIM_CLIENT_ID);
            return Optional.empty();
        }

        return Optional.of(clientId.trim());
    }
}
