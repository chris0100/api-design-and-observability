package dev.chris0100.api;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiTest {

    @Autowired MockMvc mvc;
    @Autowired MeterRegistry registry;

    @Test
    void createReturns201WithLocationAndEchoesTheCorrelationId() throws Exception {
        double before = registry.counter("orders.created").count();

        mvc.perform(post("/api/v1/orders")
                        .header("X-Correlation-Id", "test-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"c-1\",\"sku\":\"sku-9\",\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("X-Correlation-Id", "test-123"))
                .andExpect(jsonPath("$.status").value("CREATED"));

        assertEquals(before + 1, registry.counter("orders.created").count(), 0.0001);
    }

    @Test
    void invalidBodyReturnsProblemDetailWithFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":\"\",\"sku\":\"sku-9\",\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors.customerId").exists())
                .andExpect(jsonPath("$.errors.quantity").exists())
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    void unknownOrderReturns404() throws Exception {
        mvc.perform(get("/api/v1/orders/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order not found"))
                .andExpect(header().exists("X-Correlation-Id"));
    }
}
