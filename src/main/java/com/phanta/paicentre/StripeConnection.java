package com.phanta.paicentre;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@RestController
@CrossOrigin(origins = "https://sb1nxlpyh-tirr-boimpbp3--5173--c8c182a3.local-corp.webcontainer.io")
@RequestMapping("/api/stripe/connection")
public class StripeConnection {

    @Value("${stripe.client.id}")
    private String clientId;

    @Value("${stripe.redirect.uri}")
    private String redirectUri;

    @Value("${stripe.client.secret}")
    private String clientSecret;

    @Value("${stripe.webhook.endpoint}")
    private String webhookEndpoint;

    @Value("${cors.allowed.origin}")
    private String allowedOrigin;

    private static final String STRIPE_API_URL = "https://api.stripe.com/v1";

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


    @GetMapping("/oauth/callback")
    public void handleOAuthCallback(@RequestParam("code") String code, HttpServletResponse httpServletResponse) throws IOException {
        try {
            System.out.println("Received code: " + code);

            // Step 1: Exchange the authorization code for an access token
            String tokenUrl = "https://connect.stripe.com/oauth/token";
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "grant_type=authorization_code&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                    "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                    "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                assert responseBody != null;

                String accessToken = (String) responseBody.get("access_token");
                String stripeAccountId = (String) responseBody.get("stripe_user_id");

                // Step 2: Fetch account details from Stripe
                String accountUrl = "https://api.stripe.com/v1/accounts/" + stripeAccountId;

                HttpHeaders authHeaders = new HttpHeaders();
                authHeaders.setBearerAuth(accessToken);

                HttpEntity<String> accountRequest = new HttpEntity<>(authHeaders);
                ResponseEntity<Map> accountResponse = restTemplate.exchange(accountUrl, HttpMethod.GET, accountRequest, Map.class);

                if (accountResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> accountDetails = accountResponse.getBody();

                    System.out.println(accountDetails);
                    String name = accountDetails.getOrDefault("business_name", "Unknown").toString();
                    String email = accountDetails.getOrDefault("email", "Unknown").toString();

                    // URL-encode the details for redirect
                    String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
                    String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
                    String encodedAccountId = URLEncoder.encode(stripeAccountId, StandardCharsets.UTF_8);

                    // Redirect to the frontend with the account details
                    String frontendUrl = String.format(
                      //      "http://localhost:5173/settings/webhooks?status=connected&source=stripe&client_id=%s&name=%s&email=%s",
                            allowedOrigin + "/settings/webhooks?status=connected&source=stripe&client_id=%s&name=%s&email=%s",

                            encodedAccountId, encodedName, encodedEmail
                    );

                    httpServletResponse.sendRedirect(frontendUrl);
                } else {
                    throw new RuntimeException("Failed to fetch Stripe account details.");
                }
            } else {
                throw new RuntimeException("Failed to exchange authorization code for access token.");
            }
        } catch (Exception e) {
            e.printStackTrace();

            // Handle errors and redirect to the frontend with the error message
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
          //  String frontendUrl = "http://localhost:5173/settings/webhooks?status=error&source=stripe&message=" + errorMessage;
            String frontendUrl = allowedOrigin + "/settings/webhooks?status=error&source=stripe&message=" + errorMessage;

            httpServletResponse.sendRedirect(frontendUrl);
        }
    }


    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> createStripeWebhook(@RequestBody Map<String, Object> requestBody) {
        try {
            String accessToken = clientSecret; // Use your Stripe secret key as the access token

            // Get the events from the request body
            List<String> selectedEvents = (List<String>) requestBody.get("events");

            // Check if the webhook already exists
            Optional<Map<String, Object>> existingWebhook = findExistingStripeWebhook(accessToken, webhookEndpoint);

            if (existingWebhook.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "created", false,
                        "message", "Webhook already exists",
                        "webhookInfo", existingWebhook.get()
                ));
            }

            // Create the webhook if it doesn't exist
            Map<String, Object> webhookInfo = createStripeWebhook(accessToken, selectedEvents);
            return ResponseEntity.ok(Map.of(
                    "created", true,
                    "message", "Webhook created successfully",
                    "webhookInfo", webhookInfo
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "created", false,
                    "message", "Error creating webhook: " + e.getMessage()
            ));
        }
    }

    private Map<String, Object> createStripeWebhook(String accessToken, List<String> events) throws Exception {
        String url = STRIPE_API_URL + "/webhook_endpoints";
        RestTemplate restTemplate = new RestTemplate();

        // Prepare the form-encoded parameters
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("url", webhookEndpoint);  // Add the webhook endpoint URL
        for (String event : events) {
            requestBody.add("enabled_events[]", event);  // Add each event as a separate entry
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);  // Set content type to application/x-www-form-urlencoded
        headers.setBearerAuth(accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to create Stripe webhook: " + response.getStatusCode());
        }
    }


    // Helper method to check for existing webhooks
    private Optional<Map<String, Object>> findExistingStripeWebhook(String accessToken, String endpointUrl) throws Exception {
        String url = STRIPE_API_URL + "/webhook_endpoints";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            List<Map<String, Object>> webhooks = (List<Map<String, Object>>) response.getBody().get("data");

            for (Map<String, Object> webhook : webhooks) {
                if (webhook.get("url").equals(endpointUrl)) {
                    return Optional.of(webhook);
                }
            }
        }
        return Optional.empty();
    }



    @PostMapping("/webhook/delete")
    public ResponseEntity<Map<String, Object>> deleteStripeWebhook(@RequestBody Map<String, String> requestBody) {
        try {
            String webhookId = requestBody.get("webhookId");
            if (webhookId == null || webhookId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "deleted", false,
                        "message", "Webhook ID is required"
                ));
            }

            String url = STRIPE_API_URL + "/webhook_endpoints/" + webhookId;
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(clientSecret); // Use your Stripe secret key

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok(Map.of(
                        "deleted", true,
                        "message", "Webhook deleted successfully"
                ));
            } else {
                throw new RuntimeException("Failed to delete webhook: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "deleted", false,
                    "message", "Error deleting webhook: " + e.getMessage()
            ));
        }
    }

}




