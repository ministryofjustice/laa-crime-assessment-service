package uk.gov.justice.laa.crime.assessmentservice.common.api.config;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.assessmentservice.common.api.client.MaatCourtDataApiClient;
import uk.gov.justice.laa.crime.assessmentservice.common.api.filter.Resilience4jRetryFilter;
import uk.gov.justice.laa.crime.assessmentservice.common.api.filter.WebClientFilters;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
@AllArgsConstructor
public class WebClientsConfiguration {
    public static final String MAAT_API_CLIENT_NAME = "maatCourtDataWebClient";

    @Bean(MAAT_API_CLIENT_NAME)
    WebClient maatCourtDataWebClient(
            WebClient.Builder webClientBuilder,
            ServicesConfiguration servicesConfiguration,
            ClientRegistrationRepository clientRegistrations,
            OAuth2AuthorizedClientRepository authorizedClients,
            RetryRegistry retryRegistry) {

        ServletOAuth2AuthorizedClientExchangeFilterFunction oauthFilter =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients);
        oauthFilter.setDefaultClientRegistrationId(
                servicesConfiguration.getMaatApi().getRegistrationId());

        Resilience4jRetryFilter retryFilter = new Resilience4jRetryFilter(retryRegistry, MAAT_API_CLIENT_NAME);

        return webClientBuilder
                .baseUrl(servicesConfiguration.getMaatApi().getBaseUrl())
                .filters(filters -> configureFilters(filters, oauthFilter, retryFilter))
                .build();
    }

    @Bean
    MaatCourtDataApiClient maatCourtDataApiClient(
            @Qualifier("maatCourtDataWebClient") WebClient maatCourtDataWebClient) {
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(
                        WebClientAdapter.create(maatCourtDataWebClient))
                .build();
        return httpServiceProxyFactory.createClient(MaatCourtDataApiClient.class);
    }

    private void configureFilters(
            List<ExchangeFilterFunction> filters,
            ServletOAuth2AuthorizedClientExchangeFilterFunction oauthFilter,
            ExchangeFilterFunction retryFilter) {
        filters.add(WebClientFilters.logRequestHeaders());
        filters.add(retryFilter);
        filters.add(oauthFilter);
        filters.add(WebClientFilters.errorResponseHandler());
        filters.add(WebClientFilters.handleNotFoundResponse());
        filters.add(WebClientFilters.logResponse());
    }
}
