package dev.chris0100.api.order;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final Map<String, OrderResponse> store = new ConcurrentHashMap<>();
    private final Counter created;

    public OrderService(MeterRegistry registry) {
        this.created = Counter.builder("orders.created").description("Orders created").register(registry);
    }

    public OrderResponse create(CreateOrderRequest request) {
        OrderResponse order = new OrderResponse(UUID.randomUUID().toString(),
                request.customerId(), request.sku(), request.quantity(), "CREATED");
        store.put(order.id(), order);
        created.increment();
        log.info("order created id={} customer={}", order.id(), order.customerId()); // correlation id comes from the MDC
        return order;
    }

    public OrderResponse get(String id) {
        OrderResponse order = store.get(id);
        if (order == null) {
            throw new OrderNotFoundException(id);
        }
        return order;
    }
}
