package com.example.Messenger.Controller;

import com.example.Messenger.Entity.Authority;
import com.example.Messenger.Entity.User;
import com.example.Messenger.Repository.AuthorityRepository;
import com.example.Messenger.Repository.UserRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
public class ProfileController {
    private static final String ADMIN_CODE = "CHAU_XINH_DEP_2025";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;
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

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestParam String code, @RequestParam String email) {
        Optional<User> userOpt = userRepository.findUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy user!");
        }

        User user = userOpt.get();

        if (ADMIN_CODE.equals(code)) {
            Authority adminRole = authorityRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        Authority newRole = new Authority();
                        newRole.setId(UUID.randomUUID().toString());
                        newRole.setName("ROLE_ADMIN");
                        return authorityRepository.save(newRole);
                    });
            // Xóa hết quyền hiện tại
            user.getAuthorities().clear();
            // Gán quyền ROLE_ADMIN
            user.getAuthorities().add(adminRole);
            userRepository.save(user);

            return ResponseEntity.ok("✅ Gán quyền admin cho " + email + " thành công!");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("🚫 Sai mã rồi, cút!");
        }
    }


}
