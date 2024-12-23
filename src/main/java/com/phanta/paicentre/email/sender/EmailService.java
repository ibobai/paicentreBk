package com.phanta.paicentre.email.sender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    public Map<String, Object> sendVerificationEmail(String toEmail, String token) {
        String subject = "Email Verification";
        String verificationLink = "http://your-domain.com/verify?token=" + token;
        String messageText = "Please verify your email by clicking the link: " + verificationLink;

        // Construct the email payload
        Map<String, Object> emailPayload = Map.of(
                "from", "paicenter@on.resend.com", // Replace with your verified Resend sender email
                "to", toEmail,
                "subject", subject,
                "text", messageText
        );

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Create the HTTP entity
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(emailPayload, headers);

        // Send the request to the Resend API
        RestTemplate restTemplate = new RestTemplate();
        String resendApiUrl = "https://api.resend.com/emails";

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    resendApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.ACCEPTED) {
                return Map.of(
                        "send", true,
                        "message", "Email sent successfully"
                );
            } else {
                return Map.of(
                        "send", false,
                        "message", "Failed to send email: " + response.getBody()
                );
            }
        } catch (Exception e) {
            return Map.of(
                    "send", false,
                    "message", "Error sending email: " + e.getMessage()
            );
        }
    }
}
