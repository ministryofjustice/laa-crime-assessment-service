package uk.gov.justice.laa.crime.assessmentservice.audit.helper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import uk.gov.justice.laa.crime.assessmentservice.audit.internal.helper.ClientIdResolver;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class ClientIdResolverTest {

    private final ClientIdResolver resolver = new ClientIdResolver();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenNoAuthentication_whenResolveOptional_thenEmptyIsReturned() {
        Optional<String> result = resolver.resolveOptional();

        assertThat(result).isEmpty();
    }

    @Test
    void givenNoAuthentication_whenResolve_OrAnonymous_thenFallbackIsReturned() {
        assertThat(resolver.resolveOrAnonymous()).isEqualTo("anonymous");
    }

    @Test
    void givenNonJwtAuthentication_whenResolveOptional_thenEmptyIsReturned() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", "pw"));

        Optional<String> result = resolver.resolveOptional();

        assertThat(result).isEmpty();
    }

    @Test
    void givenUnauthenticatedJwtAuthentication_whenResolveOptional_thenEmptyIsReturned() {
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwtWithClaims(Map.of("client_id", "my-client")));
        SecurityContextHolder.getContext().setAuthentication(token);

        Optional<String> result = resolver.resolveOptional();

        assertThat(result).isEmpty();
        assertThat(resolver.resolveOrAnonymous()).isEqualTo("anonymous");
    }

    @Test
    void givenJwtWithoutClientIdClaim_whenResolveOptional_thenEmptyIsReturned() {
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwtWithClaims(Map.of()));
        SecurityContextHolder.getContext().setAuthentication(token);

        Optional<String> result = resolver.resolveOptional();

        assertThat(result).isEmpty();
        assertThat(resolver.resolveOrAnonymous()).isEqualTo("anonymous");
    }

    @Test
    void givenJwtWithBlankClientId_whenResolveOptional_thenEmptyIsReturned() {
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwtWithClaims(Map.of("client_id", "   ")));
        SecurityContextHolder.getContext().setAuthentication(token);

        Optional<String> result = resolver.resolveOptional();

        assertThat(result).isEmpty();
        assertThat(resolver.resolveOrAnonymous()).isEqualTo("anonymous");
    }

    @Test
    void givenJwtWithClientId_whenResolveOptional_thenClientIdIsReturned() {
        JwtAuthenticationToken token =
                new JwtAuthenticationToken(jwtWithClaims(Map.of("client_id", "crime-assessment-service")));
        SecurityContextHolder.getContext().setAuthentication(token);

        token.setAuthenticated(true);
        Optional<String> result = resolver.resolveOptional();

        assertThat(result).contains("crime-assessment-service");
        assertThat(resolver.resolveOrAnonymous()).isEqualTo("crime-assessment-service");
    }

    private Jwt jwtWithClaims(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .headers(h -> h.put("alg", "none"))
                .claims(c -> c.putAll(claims))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
