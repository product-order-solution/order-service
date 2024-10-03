package com.techie.microservices.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techie.microservices.order.dto.UserDetails;
import com.techie.microservices.order.service.JwtException;
import com.techie.microservices.order.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class JwtServiceTests {
    // Extract user details from a valid JWT token
    @Test
    public void test_extract_user_details_from_valid_jwt() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        JwtService jwtService = new JwtService(objectMapper);
        String validJwt = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJnaXZlbl9uYW1lIjoiSm9obiIsImZhbWlseV9uYW1lIjoiRG9lIn0.s5cX5Z5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q5Q";

        // Act
        UserDetails userDetails = jwtService.extractUserDetails(validJwt);

        // Assert
        assertEquals("John", userDetails.getFirstName());
        assertEquals("Doe", userDetails.getLastName());
        assertEquals("test@example.com", userDetails.getEmail());
    }

    // Handle invalid JWT token format
    @Test
    public void test_handle_invalid_jwt_token_format() {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        JwtService jwtService = new JwtService(objectMapper);
        String invalidJwt = "InvalidToken";

        // Act & Assert
        assertThrows(JwtException.class, () -> {
            jwtService.extractUserDetails(invalidJwt);
        });
    }

}
