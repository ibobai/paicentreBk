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
        String toEmail = request.get("email");
        String token = request.get("token");

        if (toEmail == null || token == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "send", false,
                    "message", "Email and token are required."
            ));
        }

        Map<String, Object> response = emailService.sendVerificationEmail(toEmail, token);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/verif")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        // Verify the token logic here
        // boolean isVerified = tokenService.verifyToken(token);

//if (isVerified) {
        if(true) {
            return ResponseEntity.ok("Email verified successfully!");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }
    }
}
