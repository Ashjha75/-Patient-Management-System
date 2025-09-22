package com.patientmanagement.patientservice.util;

import com.example.yourapp.client.dto.ExternalProductDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class FakeStoreApiClient {

    private final WebClient webClient;

    // We inject the WebClient.Builder we configured in Step 2.
    public FakeStoreApiClient(WebClient.Builder webClientBuilder) {
        // We create a specific WebClient instance for this API, setting its base URL.
        this.webClient = webClientBuilder.baseUrl("https://fakestoreapi.com").build();
    }

    /**
     * Fetches a single product by its ID.
     * This method is NON-BLOCKING and returns a "Mono", which is a publisher
     * that will eventually emit one ExternalProductDTO.
     */
    public Mono<ExternalProductDTO> getProductById(Long productId) {
        return this.webClient.get()
                .uri("/products/{id}", productId) // Appends to the base URL
                .retrieve() // Executes the request
                .bodyToMono(ExternalProductDTO.class) // Converts the response body to our DTO
                .timeout(Duration.ofSeconds(5)); // Set a specific timeout for this request
    }

    // You could add other methods here, e.g., getAllProducts(), createProduct(), etc.
}