package com.phanta.paicentre.connection.paypal.controller;

import com.phanta.paicentre.connection.paypal.service.PaypalConnectionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
@CrossOrigin(origins = " http://localhost:5173")

@RequestMapping("/api/paypal/connection")
public class PayPalConnectionController {

    @Autowired
    private final PaypalConnectionService paypalConnectionService;

    public PayPalConnectionController(PaypalConnectionService paypalConnectionService) {
        this.paypalConnectionService = paypalConnectionService;
    }

    // Step 1: Redirect to PayPal for Authorization
    // Step 1: Redirect to PayPal for Authorization
    @GetMapping("/connect")
    public void connectToPayPal(HttpServletResponse response) throws IOException {
        paypalConnectionService.connectToPayPal(response);
    }


    @GetMapping("/hello")
    public String hello(){
        return  "Hello, it's working";
    }



    // Step 2: Handle OAuth callback
    @GetMapping("/oauth/callback")
    public ResponseEntity<Void> handleOAuthCallback(@RequestParam(name = "code", required = false) String code) {
        return paypalConnectionService.handleOAuthCallback(code);
    }


    // Step 3: Create a webhook
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> createWebhook(@RequestBody Map<String, Object> payload) {
       return paypalConnectionService.createWebhook(payload);
    }


    @PostMapping("/webhook/delete")
    public ResponseEntity<Map<String, Object>> deleteWebhook(@RequestBody Map<String, String> payload) {
        return paypalConnectionService.deleteWebhook(payload);
    }

}
