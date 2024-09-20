package com.techie.microservices.order.client;



import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;


public interface InventoryClient {

    Logger log = LoggerFactory.getLogger(InventoryClient.class);
    @GetExchange("/api/inventory")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity);

    // When the inventory service fails, the order service handles this gracefully, logging errors and returning a
    // user-friendly message instead of a raw error. By returning false, the order service will just not be able to
    // place the order, and will no face degraded performance.
    default boolean fallbackMethod(String skuCode, Integer quantity, Throwable throwable) {
        log.info("fallbackMethod called with skuCode: {} and quantity: {}", skuCode, quantity, throwable);
        return false;
    }
}
