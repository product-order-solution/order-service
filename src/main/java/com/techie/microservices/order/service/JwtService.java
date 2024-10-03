package com.techie.microservices.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techie.microservices.order.dto.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final ObjectMapper objectMapper; // Inject ObjectMapper

    public UserDetails extractUserDetails(String authorizationHeader) throws JwtException {
        // Extract the JWT from the Authorization header
        String token = authorizationHeader.replace("Bearer ", "");

        try {
            // Split the token to get the payload (the second part)
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new JwtException("Invalid JWT format");
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            // Convert the payload (which is in JSON format) to a Map to extract claims
            var claims = objectMapper.readValue(payload, Map.class);

            // Extract user information from the claims
            String email = (String) claims.get("email");
            String firstName = (String) claims.get("given_name");
            String lastName = (String) claims.get("family_name");

            return new UserDetails(firstName, lastName, email);

        } catch (JsonProcessingException e) {
            throw new JwtException("Unable to parse JWT payload", e);
        }
    }
}
