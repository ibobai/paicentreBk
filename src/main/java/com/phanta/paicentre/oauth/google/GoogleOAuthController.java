package com.phanta.paicentre.oauth.google;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@CrossOrigin(origins = "https://cyan-poets-scream.loca.lt")
@RequestMapping("/api/oauth/google")
public class GoogleOAuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @GetMapping("/login")
    public ResponseEntity<String> login() {
        return googleOAuthService.login();
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        return googleOAuthService.callback(code);
    }
}
