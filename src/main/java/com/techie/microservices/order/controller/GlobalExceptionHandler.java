package com.techie.microservices.order.controller;

import com.techie.microservices.order.dto.OrderResponse;
import com.techie.microservices.order.service.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<OrderResponse> handleJwtException(JwtException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new OrderResponse("JWT error: " + e.getMessage()));
    }

    // Add more handlers if needed
}