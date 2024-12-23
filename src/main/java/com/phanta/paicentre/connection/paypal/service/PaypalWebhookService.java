package com.phanta.paicentre.connection.paypal.service;

import com.google.gson.Gson;
import com.phanta.paicentre.connection.paypal.beans.CustomAmount;
import com.phanta.paicentre.connection.paypal.beans.CustomEvent;
import com.phanta.paicentre.connection.paypal.beans.CustomResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaypalWebhookService {


    @Value("${paypal.clientId}")
    private  String CLIENT_ID;
    @Value("${paypal.clientSecret}")
    private String CLIENT_SECRET;
    @Value("${paypal.webhookID}")
    private String WEBHOOK_ID;

    private final Gson gson = new Gson();
    public void handlePayPalEvent(
            @RequestHeader("PayPal-Transmission-Sig") String transmissionSig,
            @RequestHeader("PayPal-Transmission-Id") String transmissionId,
            @RequestHeader("PayPal-Transmission-Time") String transmissionTime,
            @RequestHeader("PayPal-Cert-Url") String certUrl,
            @RequestHeader("PayPal-Auth-Algo") String authAlgo,
            @RequestBody String rawEvent) {

        System.out.println("Raw Event Payload: " + rawEvent);

        try {
//            // Step 1: Verify the webhook signature
//            String accessToken = getAccessToken();
//            boolean isValid = verifyWebhookSignature(
//                    accessToken, transmissionId, transmissionTime, certUrl, transmissionSig, authAlgo, rawEvent
//            );
//
//            if (!isValid) {
//                System.err.println("Invalid PayPal webhook signature");
//                return;
//            }
//            System.out.println("Webhook signature verified successfully!");

            // Step 2: Deserialize and handle event
            CustomEvent event = gson.fromJson(rawEvent, CustomEvent.class);
            System.out.println("Parsed Event: " + gson.toJson(event));

            // Extract event details
            String eventType = event.getEventType();
            System.out.println("Event Type: " + eventType);

            if ("PAYMENT.SALE.COMPLETED".equals(eventType)) {
                CustomResource resource = event.getResource();
                CustomAmount amount = resource.getAmount();

                System.out.println("Payment completed:");
                System.out.println(" - Sale ID: " + resource.getId());
                System.out.println(" - Amount: " + amount.getTotal() + " " + amount.getCurrency());
                System.out.println(" - State: " + resource.getState());
            } else {
                System.out.println("Unhandled event type: " + eventType);
            }
        } catch (Exception e) {
            System.err.println("Error processing webhook event: " + e.getMessage());
        }
    }



    private String getAccessToken() throws Exception {
        final String TOKEN_URL = "https://api.sandbox.paypal.com/v1/oauth2/token"; // Use live endpoint for production
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);

        HttpEntity<String> entity = new HttpEntity<>("grant_type=client_credentials", headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, String> body = response.getBody();
            return body != null ? body.get("access_token") : null;
        } else {
            throw new RuntimeException("Failed to obtain access token");
        }
    }

    private boolean verifyWebhookSignature(String accessToken, String transmissionId, String transmissionTime, String certUrl,
                                           String transmissionSig, String authAlgo, String rawEvent) {
        final String VERIFY_URL = "https://api.sandbox.paypal.com/v1/notifications/verify-webhook-signature";

        // Create request payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("transmission_id", transmissionId);
        payload.put("transmission_time", transmissionTime);
        payload.put("cert_url", certUrl);
        payload.put("auth_algo", authAlgo);
        payload.put("transmission_sig", transmissionSig);
        payload.put("webhook_id", WEBHOOK_ID);
        payload.put("webhook_event", gson.fromJson(rawEvent, Map.class)); // Parse raw event to Map

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // Send request
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(VERIFY_URL, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, String> responseBody = response.getBody();
                return responseBody != null && "VERIFIED".equals(responseBody.get("verification_status"));
            } else {
                System.err.println("Webhook verification failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
