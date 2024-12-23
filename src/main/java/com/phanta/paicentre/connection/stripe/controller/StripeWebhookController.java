package com.phanta.paicentre.connection.stripe.controller;

import com.phanta.paicentre.connection.stripe.service.StripeConnectionService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/stripe/events")
public class StripeWebhookController {

    @Autowired
    private final StripeConnectionService stripeConnectionService;

    // Constructor to initialize Stripe API key
    public StripeWebhookController(@Value("${stripe.api.key}") String apiKey, StripeConnectionService stripeConnectionService) {
        this.stripeConnectionService = stripeConnectionService;
        Stripe.apiKey = apiKey;
    }

    @GetMapping("/hello")
    public String hello(){
        return  "Hello, it's working";
    }


    @PostMapping("/event")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        //return stripeConnectionService.handleOAuthCallback(payload, sigHeader );
        return null;
    }

}
