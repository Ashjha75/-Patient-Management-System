package com.patientmanagement.patientservice.controller;

import com.patientmanagement.patientservice.dto.ExternalProductDTO;
import com.patientmanagement.patientservice.service.ExternalApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for exposing endpoints to interact with external APIs.
 */
@RestController
@RequestMapping("/api/external/products")
@Tag(
        name = "External API",
        description = "Endpoints for fetching products from the FakeStore external API"
)
public class ExternalApiController {

    private final ExternalApiService externalApiService;

    /**
     * Constructs ExternalApiController with the provided service.
     *
     * @param externalApiService the service used for external API calls
     */
    public ExternalApiController(ExternalApiService externalApiService) {
        this.externalApiService = externalApiService;
    }

    /**
     * Get a product by ID from the external API.
     *
     * @param id the product ID
     * @return a {@link Mono} emitting {@link ExternalProductDTO}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Fetches a product from the FakeStore external API by its ID."
    )
    public Mono<ExternalProductDTO> getProduct(@PathVariable Long id) {
        return externalApiService.getProductById(id);
    }
}
