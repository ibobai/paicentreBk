package com.phanta.paicentre.user;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for validating user request data.
 */
public class UserRequestValidator {

    private static final List<Boolean> ACTIVE_CONDITIONS = Arrays.asList(true, false);
    private static final List<String> ALLOWED_ROLES = Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_EDITOR");
    private static final List<String> CORRECT_SEX = Arrays.asList("f", "F", "m", "M");

    /**
     * Validates the fields of a user request object.
     *
     * @param userRequest The user request object to validate.
     * @return A ResponseEntity containing the error response if validation fails, or null if validation passes.
     */
    public static ResponseEntity<Map<String, Object>> validateUserRequest(UserRequestDTO userRequest) {
        // Validate required fields
        if (userRequest.getFirstName() == null || userRequest.getFirstName().isEmpty()) {
            return buildErrorResponse("Field 'firstName' is required.");
        }
        if (userRequest.getLastName() == null || userRequest.getLastName().isEmpty()) {
            return buildErrorResponse("Field 'lastName' is required.");
        }
        if (userRequest.getEmail() == null || userRequest.getEmail().isEmpty()) {
            return buildErrorResponse("Field 'email' is required.");
        }
        if (userRequest.getPhoneNumber() == null || userRequest.getPhoneNumber().isEmpty()) {
            return buildErrorResponse("Field 'phoneNumber' is required.");
        }
        if (userRequest.getPassword() == null || userRequest.getPassword().isEmpty()) {
            return buildErrorResponse("Field 'password' is required.");
        }
        if (userRequest.getSex() == null || userRequest.getSex().isEmpty()) {
            return buildErrorResponse("Field 'sex' is required.");
        }
        if (userRequest.getDateOfBirth() == null || userRequest.getDateOfBirth().toString().isEmpty()) {
            return buildErrorResponse("Field 'dateOfBirth' is required.");
        }

        // Validate isActive field
        if (userRequest.getIsActive() == null || !ACTIVE_CONDITIONS.contains(userRequest.getIsActive())) {
            return buildErrorResponse("Field 'isActive' is required, and must be a boolean (true or false).");
        }

        // Validate sex field
        if (!CORRECT_SEX.contains(userRequest.getSex())) {
            return buildErrorResponse("Field 'sex' must be one of the following: ('f', 'F', 'm', 'M'). 'M' for Masculine, 'F' for Feminine.");
        }

        // Validate role field
        if (userRequest.getRole() == null || userRequest.getRole().isEmpty() || !ALLOWED_ROLES.contains(userRequest.getRole())) {
            return buildErrorResponse("Field 'role' is required, and must be one of the following: (ROLE_USER, ROLE_ADMIN, ROLE_EDITOR).");
        }

        // If all validations pass, return null
        return null;
    }

    /**
     * Builds an error response as a ResponseEntity.
     *
     * @param message The error message to include in the response.
     * @return A ResponseEntity containing the error details.
     */
    private static ResponseEntity<Map<String, Object>> buildErrorResponse(String message, String path) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", "Bad Request",
                        "message", message,
                        "path", path
                )
        );
    }
    private static ResponseEntity<Map<String, Object>> buildErrorResponse(String message){
        return buildErrorResponse(message, "/api/user/createDTO");
    }
}
