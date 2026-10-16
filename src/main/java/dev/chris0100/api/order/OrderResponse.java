package dev.chris0100.api.order;

public record OrderResponse(String id, String customerId, String sku, int quantity, String status) {
}
