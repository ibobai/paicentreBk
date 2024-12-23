package com.phanta.paicentre.connection.stripe.controller;
import com.phanta.paicentre.connection.stripe.service.StripeConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
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
@CrossOrigin(origins = " http://localhost:5173")
@RequestMapping("/api/stripe/connection")
public class StripeConnectionController {

    @Autowired
    private final StripeConnectionService stripeConnectionService;

    public StripeConnectionController(StripeConnectionService stripeConnectionService) {
        this.stripeConnectionService = stripeConnectionService;
    }

    // Step 1: Redirect the user to Stripe's hosted OAuth page for onboarding
    @GetMapping("/connect")
    public void redirectToStripeOAuth(HttpServletResponse response) throws IOException {
       stripeConnectionService.redirectToStripeOAuth(response);
    }

    @GetMapping("/helloo")
    public String hello() {
        return "Hello, connection is also working!";
    }


    @GetMapping("/oauth/callback")
    public void handleOAuthCallback(@RequestParam("code") String code, HttpServletResponse httpServletResponse) throws IOException {
        stripeConnectionService.handleOAuthCallback(code, httpServletResponse);
    }


    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> createStripeWebhook(@RequestBody Map<String, Object> requestBody) {
      return  stripeConnectionService.createStripeWebhook(requestBody);
    }


    @PostMapping("/webhook/delete")
    public ResponseEntity<Map<String, Object>> deleteStripeWebhook(@RequestBody Map<String, String> requestBody) {
       return stripeConnectionService.deleteStripeWebhook(requestBody);
    }

}




