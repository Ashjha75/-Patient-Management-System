package com.patientmanagement.patientservice.dto;

/**
 * Data Transfer Object (DTO) representing a product from the FakeStore API.
 * <p>
 * Example JSON:
 * {
 * "id": 1,
 * "title": "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
 * "price": 109.95,
 * "description": "Your perfect pack for everyday use and walks in the forest...",
 * "category": "men's clothing",
 * "image": "https://fakestoreapi.com/img/81fPKd-2AYL._AC_SL1500_t.png",
 * "rating": {
 * "rate": 3.9,
 * "count": 120
 * }
 * }
 */
public record ExternalProductDTO(
        Long id,
        String title,
        Double price,
        String description,
        String category,
        String image,
        Rating rating
) {
    /**
     * Nested DTO representing the rating object inside the product JSON.
     */
    public record Rating(
            Double rate,
            Integer count
    ) {
    }
}
