package com.phanta.paicentre;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/paypal")
public class PayPalWebhookController {

    // Read values from application.properties
    @Value("${paypal.clientId}")
    private String clientId;

    @Value("${paypal.clientSecret}")
    private String clientSecret;

    @Value("${paypal.redirectUri}")
    private String redirectUri;

    @Value("${paypal.mode}")
    private String mode;


    private String accessToken; // To store the access token after authorization

    // Step 1: Redirect to PayPal for Authorization
    @GetMapping("/connect")
    public ResponseEntity<String> connectToPayPal() {
        String baseUrl = mode.equals("sandbox") ? "sandbox.paypal.com" : "paypal.com";

        String authorizationUrl = String.format(
                "https://%s/connect?client_id=%s&scope=openid%%20email%%20profile&response_type=code&redirect_uri=%s",
                baseUrl,
                clientId,
                redirectUri
        );

        return ResponseEntity.ok("<a href=\"" + authorizationUrl + "\">Connect to PayPal</a>");
    }





    @GetMapping("/hello")
    public String hello(){
        return  "Hello, it's working";
    }

    // Step 2: Handle OAuth callback
//    @GetMapping("/oauth/callback")
//    public ResponseEntity<String> handleOAuthCallback(@RequestParam("code") String code) {
//        try {
//            // Exchange authorization code for access token
//            this.accessToken = getAccessToken(code);
//
//            // Redirect to the frontend page with a query parameter indicating connection success
//            return ResponseEntity.status(HttpStatus.FOUND)
//                    .location(URI.create("http://localhost:3000/pay?status=connected"))
//                    .build();
//        } catch (Exception e) {
//            // In case of an error, redirect to the frontend with an error status
//            return ResponseEntity.status(HttpStatus.FOUND)
//                    .location(URI.create("http://localhost:3000/pay?status=error"))
//                    .build();
//        }
//    }
    @GetMapping("/oauth/callback")
    public ResponseEntity<String> handleOAuthCallback(@RequestParam("code") String code) {
        try {
            // Exchange authorization code for access token
            this.accessToken = getAccessToken(code);

            // Create a JavaScript response to post a message to the opener
            String successMessage =
                    "<script>" +
                            "  window.opener.postMessage(" +
                            "    { type: 'PAYPAL_CONNECTED', client_id: '" + clientId + "' }," +
                            "    window.opener.location.origin" +
                            "  );" +
                            "  window.close();" +
                            "</script>";

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(successMessage);

        } catch (Exception e) {
            // In case of an error, create a JavaScript response to notify the opener
            String errorMessage =
                    "<script>" +
                            "  window.opener.postMessage(" +
                            "    { type: 'PAYPAL_ERROR', error: '" + e.getMessage() + "' }," +
                            "    window.opener.location.origin" +
                            "  );" +
                            "  window.close();" +
                            "</script>";

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(errorMessage);
        }
    }



    // Step 3: Create a webhook
    @PostMapping("/webhook")
    public ResponseEntity<String> createWebhook() {
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated with PayPal.");
        }

        try {
            String webhookId = createPayPalWebhook();
            return ResponseEntity.ok("Webhook created successfully with ID: " + webhookId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating webhook: " + e.getMessage());
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

    // Helper Method: Create PayPal Webhook
    private String createPayPalWebhook() throws Exception {
        String webhookUrl = mode.equals("sandbox") ?
                "https://api.sandbox.paypal.com/v1/notifications/webhooks" :
                "https://api.paypal.com/v1/notifications/webhooks";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken); // Use Bearer Auth with the access token

        Map<String, Object> payload = new HashMap<>();
        payload.put("url", "https://your-webhook-endpoint.com/webhook"); // Replace with your actual webhook URL
        payload.put("event_types", new Object[]{
                Map.of("name", "PAYMENT.SALE.COMPLETED"),
                Map.of("name", "BILLING.SUBSCRIPTION.CREATED")
        });

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.exchange(webhookUrl, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("id")) {
                return (String) responseBody.get("id");
            } else {
                throw new RuntimeException("No webhook ID in PayPal response.");
            }
        } else {
            throw new RuntimeException("Failed to create webhook: " + response.getStatusCode());
        }
    }
}
