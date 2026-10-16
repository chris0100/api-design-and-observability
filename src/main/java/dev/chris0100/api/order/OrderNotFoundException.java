package dev.chris0100.api.order;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String id) {
        super("order " + id + " not found");
    }
}
