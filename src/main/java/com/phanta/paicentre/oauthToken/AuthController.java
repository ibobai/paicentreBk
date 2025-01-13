package com.phanta.paicentre.oauthToken;

import com.phanta.paicentre.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    public AuthController(JwtTokenUtil jwtTokenUtil, UserService userService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    /**
     * Login endpoint to authenticate the user using Basic Auth and return tokens.
     *
     * @param headers The HTTP headers containing the Basic Auth credentials
     * @return ResponseEntity with login status and tokens if successful
     */
    @GetMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract credentials from Basic Auth
        if (authorizationHeader != null && authorizationHeader.startsWith("Basic ")) {
            // Decode Base64 encoded credentials
            String base64Credentials = authorizationHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] values = credentials.split(":", 2);

            // Ensure the format is correct (email:password)
            if (values.length == 2) {
                String email = values[0];
                String password = values[1];

                // Call the service to handle the authentication logic
                return userService.login(email, password);
            }
        }
        // If credentials are missing or invalid, return 401 Unauthorized
        Map<String, Object> response = new HashMap<>();
        response.put("valid", false);
        response.put("message", "Invalid or missing Authorization header");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }


    // Refresh Token Endpoint
    // Refresh Token Endpoint
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract the token from the header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header");
        }

        String refreshToken = authorizationHeader.substring(7); // Extract token after "Bearer "

        // Validate the refresh token
        if (jwtTokenUtil.validateToken(refreshToken)) {
            String email = jwtTokenUtil.extractEmail(refreshToken); // Extract the email (subject) from the token
            String role = jwtTokenUtil.extractRole(refreshToken); // Optionally extract role if needed

            // Generate a new access token
            String newAccessToken = jwtTokenUtil.generateAccessToken(email, role);

            // Return the new access token
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        }

        // Return 401 if the token is invalid
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }

}
