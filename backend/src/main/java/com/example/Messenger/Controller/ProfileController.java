package com.example.Messenger.Controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ProfileController {

    @PostMapping("/ask")
    public ResponseEntity<String> askFastApi(@RequestBody Map<String, String> requestBody) {
        RestTemplate restTemplate = new RestTemplate();

        // ✅ Sửa lại URL cho đúng với FastAPI
        String fastApiUrl = "http://localhost:8000/generate";

        String text = requestBody.get("text");

        Map<String, String> payload = new HashMap<>();
        payload.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // ✅ Vì FastAPI dùng Form(...)

        // ✅ Chuyển payload thành dạng URL-encoded
        String body = "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, requestEntity, Map.class);

        Object answer = response.getBody().get("response");

        return ResponseEntity.ok(answer.toString());
    }
}
