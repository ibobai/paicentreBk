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
@CrossOrigin(origins = "https://f70d42e81fb49eb8b38f8b3a7e802cff.serveo.net")
@RequestMapping("/api/oauth/google")
public class GoogleOAuthController {

    @Autowired
    private GoogleOAuthConfig googleOAuthConfig;

    private final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @GetMapping("/login")
    public ResponseEntity<String> login() {
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + googleOAuthConfig.getClientId() +
                "&redirect_uri=" + googleOAuthConfig.getRedirectUri() +
                "&response_type=code" +
                "&scope=email%20profile";
        return ResponseEntity.ok(authUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        // Exchange the code for an access token
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> requestBody = Map.of(
                "code", code,
                "client_id", googleOAuthConfig.getClientId(),
                "client_secret", googleOAuthConfig.getClientSecret(),
                "redirect_uri", googleOAuthConfig.getRedirectUri(),
                "grant_type", "authorization_code"
        );

        try {
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(TOKEN_URL, requestBody, Map.class);
            Map<String, Object> tokenData = tokenResponse.getBody();

            assert tokenData != null;
            String accessToken = (String) tokenData.get("access_token");

            // Fetch user profile using the access token
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, entity, Map.class);
            Map<String, Object> userInfo = userInfoResponse.getBody();

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error during Google OAuth: " + e.getMessage());
        }
    }
}
