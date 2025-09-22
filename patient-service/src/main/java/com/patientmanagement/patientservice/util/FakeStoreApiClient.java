package com.patientmanagement.patientservice.util;

import com.patientmanagement.patientservice.dto.ExternalProductDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client for interacting with the FakeStore API.
 * <p>
 * This uses Spring WebFlux's WebClient to fetch data from the external API.
 */
@Component
public class FakeStoreApiClient {

    private final WebClient webClient;

    /**
     * Constructs a new FakeStoreApiClient with a base URL of the FakeStore API.
     *
     * @param builder Spring-provided WebClient.Builder (auto-configured as a bean).
     */
    public FakeStoreApiClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://fakestoreapi.com")
                .build();
    }

    /**
     * Fetches a product by its ID.
     *
     * @param id the product ID
     * @return a {@link Mono} emitting {@link ExternalProductDTO}
     */
    public Mono<ExternalProductDTO> getProductById(Long id) {
        return webClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .bodyToMono(ExternalProductDTO.class);
    }
}
