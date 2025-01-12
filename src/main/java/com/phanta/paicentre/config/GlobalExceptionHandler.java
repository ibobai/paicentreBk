package com.phanta.paicentre.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Utility method to build a JSON error response.
     */
    private Map<String, Object> buildErrorResponse(int status, String error, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    /**
     * Handler for 404 (Not Found) errors.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoHandlerFoundException ex) {
        String path = ex.getRequestURL(); // Extract the requested URL
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "The requested URL was not found on the server.",
                path
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI(); // Get the URL causing the error
        Map<String, Object> errorResponse = new HashMap<>();

        // Check if it's a database-related exception
        if (ex instanceof DataIntegrityViolationException || ex instanceof InvalidDataAccessApiUsageException) {
            // Log the error to the console (or to a logger) for debugging purposes
            ex.printStackTrace();  // You can also use a logging framework like SLF4J instead of printStackTrace

            // Set a generic error message for the client
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "An error occurred while performing actions in the Database.");
        } else {
            // Handle other exceptions normally
            errorResponse = buildErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    "An unexpected error occurred: " + ex.getMessage(),
                    path
            );
        }

        // Add timestamp and path
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", path);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    /**
     * Handler for unauthorized or forbidden access (403).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String path = request.getRequestURI(); // Get the URL causing the error
        Map<String, Object> errorResponse = buildErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Access Denied: " + ex.getMessage(),
                path
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParseException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        // Build the error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", "Invalid parameter value provided. For boolean fields, only true or false are allowed. Strings like 'true' or 'false' are not accepted. " +
                "For 'latitude' and 'longitude', values must be numbers (e.g., 2433.544). For 'taxPercentage', values must be a number or double.");
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("timestamp", LocalDateTime.now());

        return ResponseEntity.badRequest().body(errorResponse);
    }

}
