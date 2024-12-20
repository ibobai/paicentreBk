package com.phanta.paicentre;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "https://sb1nxlpyh-tirr--5173--c8c182a3.local-corp.webcontainer.io")

@RequestMapping("/api/paypal/connection")
public class PayPalConnection {

    // Read values from application.properties
    @Value("${paypal.clientId}")
    private String clientId;

    @Value("${paypal.clientSecret}")
    private String clientSecret;

    @Value("${paypal.redirectUri}")
    private String redirectUri;

    @Value("${paypal.mode}")
    private String mode;

    @Value("${cors.allowed.origin}")
    private String allowedOrigin;

    @Value("${paypal.webhook.endpoint}")
    private String webhookEndpoint;

    private String accessToken; // To store the access token after authorization

    // Step 1: Redirect to PayPal for Authorization
    // Step 1: Redirect to PayPal for Authorization
    @GetMapping("/connect")
    public void connectToPayPal(HttpServletResponse response) throws IOException {
        String baseUrl = mode.equals("sandbox") ? "sandbox.paypal.com" : "paypal.com";

        String authorizationUrl = String.format(
                "https://%s/connect?client_id=%s&scope=openid%%20email%%20profile&response_type=code&redirect_uri=%s",
                baseUrl,
                clientId,
                redirectUri
        );

        // Redirect the user directly to the authorization URL
        response.sendRedirect(authorizationUrl);
    }




    @GetMapping("/hello")
    public String hello(){
        return  "Hello, it's working";
    }

    // Step 2: Handle OAuth callback
    @GetMapping("/oauth/callback")
    public ResponseEntity<Void> handleOAuthCallback(@RequestParam(name = "code", required = false) String code) {
        try {
            if (code == null || code.isEmpty()) {
                throw new IllegalArgumentException("Missing authorization code.");
            }

            // Exchange the authorization code for an access token
            this.accessToken = getAccessToken(code);

            // Fetch user details using the access token
            Map<String, String> userDetails = getUserDetails();

            // Extract and URL-encode the required details
            String name = URLEncoder.encode(userDetails.getOrDefault("name", "Unknown"), StandardCharsets.UTF_8);
            String email = URLEncoder.encode(userDetails.getOrDefault("email", "Unknown"), StandardCharsets.UTF_8);
            String payerId = URLEncoder.encode(userDetails.getOrDefault("payer_id", "Unknown"), StandardCharsets.UTF_8);

            // Construct the redirect URL with payer_id, name, and email
            String frontendUrl = String.format(
                    // "http://localhost:5173/settings/webhooks?status=connected&source=paypal&client_id=%s&name=%s&email=%s",
                    allowedOrigin +  "/settings/webhooks?status=connected&source=paypal&client_id=%s&name=%s&email=%s",
                    payerId, name, email
            );
            System.out.println(frontendUrl);

            // Redirect to the frontend URL
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();

            // Handle error and redirect to error page with a message
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
          //  String frontendErrorUrl = "http://localhost:5173/settings/webhooks?status=error&source=paypal&message=" + errorMessage;
            String frontendErrorUrl = allowedOrigin + "/settings/webhooks?status=error&source=paypal&message=" + errorMessage;

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendErrorUrl))
                    .build();
        }
    }



    private Map<String, String> getUserDetails() throws Exception {
        String userInfoUrl = mode.equals("sandbox") ?
                "https://api.sandbox.paypal.com/v1/identity/oauth2/userinfo?schema=paypalv1.1" :
                "https://api.paypal.com/v1/identity/oauth2/userinfo?schema=paypalv1.1";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            System.out.println("Response Body: " + responseBody);

            if (responseBody != null) {
                // Extract name
                String name = responseBody.getOrDefault("name", "Unknown").toString();

                // Extract email from the 'emails' list
                String email = "Unknown";
                List<Map<String, Object>> emails = (List<Map<String, Object>>) responseBody.get("emails");
                if (emails != null && !emails.isEmpty()) {
                    Map<String, Object> emailEntry = emails.get(0);
                    email = emailEntry.getOrDefault("value", "Unknown").toString();
                }

                // Extract payer_id
                String payerId = responseBody.getOrDefault("payer_id", "Unknown").toString();

                // Populate the user details map
                Map<String, String> userDetails = new HashMap<>();
                userDetails.put("name", name);
                userDetails.put("email", email);
                userDetails.put("payer_id", payerId); // Add the payer_id to the map

                return userDetails;
            } else {
                throw new RuntimeException("Failed to fetch user details from PayPal.");
            }
        } else {
            throw new RuntimeException("Error fetching user details: " + response.getStatusCode());
        }
    }


    // Helper Method: Exchange authorization code for access token
    private String getAccessToken(String authorizationCode) throws Exception {
        String tokenUrl = mode.equals("sandbox") ?
                "https://api.sandbox.paypal.com/v1/oauth2/token" :
                "https://api.paypal.com/v1/oauth2/token";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret); // Set Basic Auth with client ID and secret

        String body = "grant_type=authorization_code&code=" + authorizationCode + "&redirect_uri=" + redirectUri;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("access_token")) {
                return (String) responseBody.get("access_token");
            } else {
                throw new RuntimeException("No access_token in PayPal response.");
            }
        } else {
            throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
        }
    }




    // Step 3: Create a webhook
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> createWebhook(@RequestBody Map<String, Object> payload) {
        try {
            List<String> eventTypes = (List<String>) payload.get("events");

            if (eventTypes == null || eventTypes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No event types specified."));
            }

            // Get a fresh access token for webhook creation
            String merchantAccessToken = getAccessTokenForApiCalls();

            // Check if the webhook already exists
            Optional<Map<String, Object>> existingWebhook = findExistingWebhook(merchantAccessToken, webhookEndpoint);
            if (existingWebhook.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "created", false,
                        "message", "Webhook already exists",
                        "webhookInfo", existingWebhook.get()
                ));
            }

            // Create the webhook if it doesn't exist
            Map<String, Object> webhookInfo = createPayPalWebhook(eventTypes, merchantAccessToken);
            return ResponseEntity.ok(Map.of(
                    "created", true,
                    "message", "Webhook created successfully",
                    "webhookInfo", webhookInfo
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error creating webhook: " + e.getMessage()));
        }
    }


    // Helper Method: Get access token using Client Credentials grant type for API calls
    private String getAccessTokenForApiCalls() throws Exception {
        String tokenUrl = mode.equals("sandbox") ?
                "https://api.sandbox.paypal.com/v1/oauth2/token" :
                "https://api.paypal.com/v1/oauth2/token";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret); // Use Basic Auth with client ID and secret

        String body = "grant_type=client_credentials"; // Client Credentials grant type
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("access_token")) {
                return (String) responseBody.get("access_token");
            } else {
                throw new RuntimeException("No access_token in PayPal response.");
            }
        } else {
            throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
        }
    }

    // Helper Method: Create PayPal Webhook
    private Map<String, Object> createPayPalWebhook(List<String> eventTypes, String merchantAccessToken) throws Exception {
        String webhookUrl = mode.equals("sandbox") ?
                "https://api.sandbox.paypal.com/v1/notifications/webhooks" :
                "https://api.paypal.com/v1/notifications/webhooks";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(merchantAccessToken); // Use the merchant-level access token

        // Prepare the list of event types
        List<Map<String, String>> eventTypeList = eventTypes.stream()
                .map(type -> Map.of("name", type))
                .toList();

        Map<String, Object> payload = new HashMap<>();
        payload.put("url", webhookEndpoint); // Replace with your actual webhook URL
        payload.put("event_types", eventTypeList);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.exchange(webhookUrl, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                return responseBody;
            } else {
                throw new RuntimeException("No webhook details in PayPal response.");
            }
        } else {
            throw new RuntimeException("Failed to create webhook: " + response.getStatusCode());
        }
    }

    private Optional<Map<String, Object>> findExistingWebhook(String accessToken, String webhookUrl) throws Exception {
        String listWebhooksUrl = mode.equals("sandbox") ?
                "https://api.sandbox.paypal.com/v1/notifications/webhooks" :
                "https://api.paypal.com/v1/notifications/webhooks";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(listWebhooksUrl, HttpMethod.GET, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            List<Map<String, Object>> webhooks = (List<Map<String, Object>>) response.getBody().get("webhooks");
            for (Map<String, Object> webhook : webhooks) {
                if (webhookUrl.equals(webhook.get("url"))) {
                    return Optional.of(webhook);
                }
            }
        }

        return Optional.empty();
    }


    @PostMapping("/webhook/delete")
    public ResponseEntity<Map<String, Object>> deleteWebhook(@RequestBody Map<String, String> payload) {
        try {
            String webhookId = payload.get("webhookId");

            if (webhookId == null || webhookId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "deleted", false,
                        "message", "Webhook ID is required."
                ));
            }

            // Call the helper method to delete the webhook
            HttpStatus responseStatus = deletePayPalWebhook(webhookId);

            if (responseStatus == HttpStatus.NO_CONTENT) {  // 204 No Content for successful deletion
                return ResponseEntity.ok(Map.of(
                        "deleted", true,
                        "message", "Webhook deleted successfully"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "deleted", false,
                        "message", "Failed to delete webhook: " + responseStatus
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "deleted", false,
                    "message", "Error deleting webhook: " + e.getMessage()
            ));
        }
    }



    private HttpStatus deletePayPalWebhook(String webhookId) throws Exception {
        String webhookUrl = mode.equals("sandbox") ?
                "https://api.sandbox.paypal.com/v1/notifications/webhooks/" + webhookId :
                "https://api.paypal.com/v1/notifications/webhooks/" + webhookId;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessTokenForApiCalls());

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(webhookUrl, HttpMethod.DELETE, request, Void.class);

        return HttpStatus.valueOf(response.getStatusCode().value());  // Cast to HttpStatus
    }


}
