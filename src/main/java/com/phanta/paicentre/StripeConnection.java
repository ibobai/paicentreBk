package com.phanta.paicentre;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stripe/connection")
public class StripeConnection {

    @Value("${stripe.client.id}")
    private String clientId;

    @Value("${stripe.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    // Step 1: Redirect the user to Stripe's hosted OAuth page for onboarding
    @GetMapping("/connect")
    public void redirectToStripeOAuth(HttpServletResponse response) throws Exception {
        // Stripe's hosted onboarding URL
        String stripeOAuthUrl = "https://connect.stripe.com/oauth/authorize";

        // Build the full URL for the OAuth redirect
        String authorizationUrl = UriComponentsBuilder.fromHttpUrl(stripeOAuthUrl)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)  // This is required to initiate OAuth
                .queryParam("scope", "read_only")
                .queryParam("redirect_uri", redirectUri)
                .toUriString();

        // Redirect the user to Stripe's OAuth page
        response.sendRedirect(authorizationUrl);
    }

    @GetMapping("/helloo")
    public String hello(){
        return  "Hello, from connection is also working";
    }

    // Step 2: Handle the OAuth callback and retrieve the authorization code
    @GetMapping("/oauth/callback")
    public ResponseEntity<String> handleOAuthCallback(@RequestParam("code") String code) {
        try {

            System.out.println("The redirect uri : " + redirectUri);
            // Exchange the authorization code for an access token
            String tokenUrl = "https://connect.stripe.com/oauth/token";

            Map<String, String> params = new HashMap<>();
            params.put("grant_type", "authorization_code");
            params.put("code", code);
            params.put("client_id", clientId); // Your Stripe client ID (from Stripe dashboard)
            params.put("redirect_uri", redirectUri);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

            // Send the request to Stripe to get the access token
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String stripeUserId = (String) responseBody.get("stripe_user_id");

                // Send success message back to the frontend with stripe_user_id
                String successMessage =
                        "<!DOCTYPE html>" +
                                "<html>" +
                                "<body>" +
                                "<script>" +
                                "  console.log('Sending STRIPE_CONNECTED message');" +
                                "  window.opener.postMessage({ " +
                                "    type: 'STRIPE_CONNECTED', " +
                                "    stripe_user_id: '" + stripeUserId + "' " +
                                "  }, window.opener.location.origin);" +
                                "  window.close();" +
                                "</script>" +
                                "</body>" +
                                "</html>";

                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(successMessage);
            } else {
                throw new RuntimeException("Failed to connect with Stripe");
            }

        } catch (Exception e) {
            // Handle error if something goes wrong
            String errorMessage =
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<body>" +
                            "<script>" +
                            "  console.error('Error:', '" + e.getMessage() + "');" +
                            "  window.opener.postMessage({ type: 'STRIPE_ERROR', error: '" + e.getMessage() + "' }, window.opener.location.origin);" +
                            "  window.close();" +
                            "</script>" +
                            "</body>" +
                            "</html>";

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(errorMessage);
        }
    }
}
