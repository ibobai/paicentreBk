package com.phanta.paicentre;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/stripe/connection")
public class StripeConnection {

    @Value("${stripe.client.id}")
    private String clientId;

    @Value("${stripe.redirect.uri}")
    private String redirectUri;

    // Step 1: Redirect the user to Stripe's hosted OAuth page for onboarding
    @GetMapping("/connect")
    public void redirectToStripeOAuth(HttpServletResponse response) throws IOException {
        // Stripe's hosted onboarding URL
        String stripeOAuthUrl = "https://connect.stripe.com/oauth/authorize";

        // Build the full URL for the OAuth redirect
        String authorizationUrl = UriComponentsBuilder.fromHttpUrl(stripeOAuthUrl)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId) // Your Stripe client ID
                .queryParam("scope", "read_write")
                .queryParam("redirect_uri", redirectUri) // Your registered redirect URI
                .toUriString();

        // Redirect the user to Stripe's OAuth page
        response.sendRedirect(authorizationUrl);
    }

    @GetMapping("/helloo")
    public String hello() {
        return "Hello, connection is also working!";
    }

    // Step 2: Handle the OAuth callback and pass the code to the frontend
    // Step 2: Handle the OAuth callback and pass the code to the frontend
    @GetMapping("/oauth/callback")
    public void handleOAuthCallback(@RequestParam("code") String code, HttpServletResponse httpServletResponse) throws IOException {
        try {
            System.out.println("The redirect URI: " + redirectUri);
            System.out.println("Received code: " + code);

            // Set frontend URL as localhost for local development
            String frontendUrl = "http://localhost:3000/str?status=connected&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);

            // Redirect to the frontend URL
            httpServletResponse.sendRedirect(frontendUrl);
        } catch (IOException e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);

            // Error handling: redirect to error page on the frontend
            String frontendUrl = "http://localhost:3000/str?status=error&message=" + errorMessage;

            // Redirect to the error page on the frontend
            httpServletResponse.sendRedirect(frontendUrl);
        }
    }

}
