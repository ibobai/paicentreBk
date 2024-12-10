package com.phanta.paicentre;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/stripe/controller")
public class StripeWebhookController {

    @Value("${stripe.api.key}")
    private String apiKey;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    // Constructor to initialize Stripe API key
    public StripeWebhookController(@Value("${stripe.api.key}") String apiKey) {
        Stripe.apiKey = apiKey;
    }

    @GetMapping("/hello")
    public String hello(){
        return  "Hello, it's working";
    }
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        String eventType;
        Event event;

        try {
            // Verify the webhook signature
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            eventType = event.getType();
            System.out.println("Signature verified successfully.");
        } catch (SignatureVerificationException e) {
            // Invalid signature
            System.out.println("Webhook signature verification failed.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook signature verification failed.");
        }

        // Handle the event based on its type
        switch (eventType) {
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "invoice.payment_succeeded":
                handleInvoicePaymentSucceeded(event);
                break;
            case "charge.succeeded":
                handleChargeSucceeded(event);
                break;
            default:
                // Handle unexpected event types
                System.out.println("Unhandled event type: " + eventType);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unhandled event type: " + eventType);
        }
        return ResponseEntity.ok("Webhook received successfully.");
    }

    private void handlePaymentIntentSucceeded(Event event) {
        Optional<StripeObject> optionalPaymentIntent = event.getDataObjectDeserializer().getObject();

        if (optionalPaymentIntent.isPresent()) {
            PaymentIntent paymentIntent = (PaymentIntent) optionalPaymentIntent.get();
            String paymentId = paymentIntent.getId();
            Long amount = paymentIntent.getAmountReceived();
            String currency = paymentIntent.getCurrency();
            String customerId = paymentIntent.getCustomer();

            System.out.println("Payment Succeeded:");
            System.out.println(" - Payment ID: " + paymentId);
            System.out.println(" - Amount: " + amount + " " + currency);
            if (customerId != null) {
                System.out.println(" - Customer ID: " + customerId);
            } else {
                System.out.println(" - Customer ID not available.");
            }
        } else {
            System.out.println("PaymentIntent not found in the event.");
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalArgumentException("Event data missing"));

        String sessionId = session.getId();
        String customerEmail = session.getCustomerDetails().getEmail();
        String customerId = session.getCustomer();

        System.out.println("Checkout Session Completed:");
        System.out.println(" - Session ID: " + sessionId);
        System.out.println(" - Customer Email: " + customerEmail);
        if (customerId != null) {
            System.out.println(" - Customer ID: " + customerId);
        } else {
            System.out.println(" - Customer ID not available in session.");
        }
    }

    private void handleInvoicePaymentSucceeded(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalArgumentException("Event data missing"));

        String invoiceId = invoice.getId();
        Long amountPaid = invoice.getAmountPaid();
        String currency = invoice.getCurrency();
        String customerId = invoice.getCustomer();

        System.out.println("Invoice Payment Succeeded:");
        System.out.println(" - Invoice ID: " + invoiceId);
        System.out.println(" - Amount Paid: " + amountPaid + " " + currency);
        if (customerId != null) {
            System.out.println(" - Customer ID: " + customerId);
        } else {
            System.out.println(" - Customer ID not available.");
        }
    }

    private void handleChargeSucceeded(Event event) {
        Optional<StripeObject> optionalCharge = event.getDataObjectDeserializer().getObject();

        if (optionalCharge.isPresent()) {
            Charge charge = (Charge) optionalCharge.get();
            String chargeId = charge.getId();
            Long amount = charge.getAmount();
            String currency = charge.getCurrency();
            String customerId = charge.getCustomer();

            System.out.println("Charge Succeeded:");
            System.out.println(" - Charge ID: " + chargeId);
            System.out.println(" - Amount: " + amount + " " + currency);
            if (customerId != null) {
                System.out.println(" - Customer ID: " + customerId);
            } else {
                System.out.println(" - Customer ID not available in charge.");
            }
        } else {
            System.out.println("Charge object not found in the event.");
        }
    }
}
