package com.phanta.paicentre.oauthToken;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

    // Generate Access Token
    public String generateAccessToken(String email, String role) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY); // Using HMAC256 for signing the JWT

        return JWT.create()
                .withSubject(email)  // Subject (username or email)
                .withClaim("role", role)  // Add role as a claim
                .withIssuedAt(new Date())  // Set issue time
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))  // Set expiration time
                .sign(algorithm);  // Sign the token using HMAC256 algorithm
    }

    // Generate Refresh Token
    public String generateRefreshToken(String email) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

        return JWT.create()
                .withSubject(email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .sign(algorithm);
    }

    // Validate Token
    public boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWT.require(algorithm).build().verify(token);  // Verifies token signature
            return true;
        } catch (Exception e) {
            return false;  // Invalid or expired token
        }
    }

    // Extract Username (email) from the token
    public String extractEmail(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);  // Decode the token without validating it
        return decodedJWT.getSubject();  // Get the subject (username/email)
    }

    // Extract Role from the token
    public String extractRole(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getClaim("role").asString();  // Get role claim
    }
}
