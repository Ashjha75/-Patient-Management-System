package com.patientmanagement.patientservice.service;

import com.patientmanagement.patientservice.dto.ExternalProductDTO;
import reactor.core.publisher.Mono;

/**
 * Service interface for fetching products from external APIs.
 */
public interface ExternalApiService {

    /**
     * Fetch a product from the external API by ID.
     *
     * @param id the product ID
     * @return a {@link Mono} emitting {@link ExternalProductDTO}
     */
    Mono<ExternalProductDTO> getProductById(Long id);
}
