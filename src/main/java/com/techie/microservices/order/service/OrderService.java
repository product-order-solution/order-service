package com.techie.microservices.order.service;

import com.techie.microservices.order.client.InventoryClient;
import com.techie.microservices.order.dto.OrderRequest;
import com.techie.microservices.order.event.OrderPlacedEvent;
import com.techie.microservices.order.model.Order;
import com.techie.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String kafkaTopic;

    public void placeOrder(OrderRequest orderRequest) {
        boolean inStock = inventoryClient.isInStock(orderRequest.getSkuCode(), orderRequest.getQuantity());
        if (inStock){
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setSkuCode(orderRequest.getSkuCode());
            order.setPrice(orderRequest.getPrice());
            order.setQuantity(orderRequest.getQuantity());
            orderRepository.save(order);

            // Send the message to Kafka Topic
            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(order.getOrderNumber(),
                    orderRequest.getUserDetails().getFirstName(), orderRequest.getUserDetails().getLastName(),
                    orderRequest.getUserDetails().getEmail());
            log.info("Start - Sending OrderPlacedEvent {} to Kafka topic {}", orderPlacedEvent, kafkaTopic);
            kafkaTemplate.send(kafkaTopic, orderPlacedEvent);
            log.info("End - Sending OrderPlacedEvent {} to Kafka topic {}", orderPlacedEvent, kafkaTopic);
        } else {
            throw new RuntimeException("Product with SkuCode " + orderRequest.getSkuCode() + " is not in stock");
        }

    }
}
