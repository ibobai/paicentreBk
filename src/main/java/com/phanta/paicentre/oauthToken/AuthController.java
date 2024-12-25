package com.phanta.paicentre.oauthToken;

import com.phanta.paicentre.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Login endpoint to authenticate the user and return tokens
     *
     * @param credentials A map containing email and password
     * @return ResponseEntity with login status and tokens if successful
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        // Call to the service to handle the authentication logic
        return userService.login(email, password);
    }

    // Refresh Token Endpoint
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> tokenRequest) {
        String refreshToken = tokenRequest.get("refreshToken");

        if (jwtTokenUtil.validateToken(refreshToken)) {
            String email = jwtTokenUtil.extractEmail(refreshToken);
            String newAccessToken = jwtTokenUtil.generateAccessToken(email, "ROLE_USER"); // Adjust role extraction if needed
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }
}
