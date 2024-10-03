package com.techie.microservices.order.controller;

import com.techie.microservices.order.dto.OrderRequest;
import com.techie.microservices.order.dto.OrderResponse;
import com.techie.microservices.order.dto.UserDetails;
import com.techie.microservices.order.service.JwtException;
import com.techie.microservices.order.service.JwtService;
import com.techie.microservices.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtService jwtService; // New service to handle JWT
    private final ObjectMapper objectMapper; // Injected as a Spring-managed bean

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestBody OrderRequest orderRequest,
            @RequestHeader("Authorization") String authorizationHeader) {

        try {
            // Extract user details from the JWT using JwtService
            UserDetails userDetails = jwtService.extractUserDetails(authorizationHeader);

            // Log the user information
            log.info("Placing order for user: {} {} (Email: {})",
                    userDetails.getFirstName(), userDetails.getLastName(), userDetails.getEmail());

            // Set the user details in the order request
            orderRequest.setUserDetails(userDetails.getFirstName(), userDetails.getLastName(), userDetails.getEmail());

            // Call the OrderService to place the order
            orderService.placeOrder(orderRequest);

            // Return a success response with details
            OrderResponse response = new OrderResponse("Order placed successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (JwtException e) {
            // If JWT is malformed or invalid
            log.error("Invalid JWT: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new OrderResponse("Invalid JWT: " + e.getMessage()));
        }
    }
}
