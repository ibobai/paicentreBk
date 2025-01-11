package com.phanta.paicentre.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("path", request.getRequestURI());

        if (authException != null) {
            // For authentication failure (invalid or missing token)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            responseBody.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            responseBody.put("error", "Unauthorized");
            responseBody.put("message", "Access Denied: Invalid or Missing Token");
        } else {
            // Fallback for other errors (e.g., URL not found)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404 Not Found
            responseBody.put("status", HttpServletResponse.SC_NOT_FOUND);
            responseBody.put("error", "Not Found");
            responseBody.put("message", "The requested URL was not found on the server.");
        }

        // Set response type to JSON
        response.setContentType("application/json");

        // Convert the response body to a JSON string
        String jsonResponse = objectMapper.writeValueAsString(responseBody);
        response.getWriter().write(jsonResponse);
    }
}
