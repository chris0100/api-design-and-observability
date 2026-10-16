package dev.chris0100.api.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotBlank String sku,
        @Positive @Max(100) int quantity) {
}
