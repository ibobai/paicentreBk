package com.phanta.paicentre.email.sender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send/verification")
    public ResponseEntity<Map<String, Object>> sendVerificationEmail(@RequestBody Map<String, String> request) {
        return emailService.sendVerificationEmail(request);
    }

    @GetMapping("/verif")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        return  emailService.verifyEmail(token);
    }
}
