package com.phanta.paicentre.connection.paypal.controller;



import com.phanta.paicentre.connection.paypal.service.PaypalWebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paypal/events")
public class PayPalWebhookController {


    @Autowired
    private final PaypalWebhookService paypalWebhookService;

    public PayPalWebhookController(PaypalWebhookService paypalWebhookService) {
        this.paypalWebhookService = paypalWebhookService;
    }

    @PostMapping("/event")
    public void handlePayPalEvent(
            @RequestHeader("PayPal-Transmission-Sig") String transmissionSig,
            @RequestHeader("PayPal-Transmission-Id") String transmissionId,
            @RequestHeader("PayPal-Transmission-Time") String transmissionTime,
            @RequestHeader("PayPal-Cert-Url") String certUrl,
            @RequestHeader("PayPal-Auth-Algo") String authAlgo,
            @RequestBody String rawEvent) {

        paypalWebhookService.handlePayPalEvent(transmissionSig, transmissionId, transmissionTime,
                certUrl, authAlgo, rawEvent);

    }

}