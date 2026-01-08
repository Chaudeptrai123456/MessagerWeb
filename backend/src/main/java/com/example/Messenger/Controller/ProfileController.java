package com.example.Messenger.Controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.Messenger.Entity.Authority;
import com.example.Messenger.Entity.User;
import com.example.Messenger.Record.UserProfile;
import com.example.Messenger.Record.UserProfileResponse;
import com.example.Messenger.Repository.AuthorityRepository;
import com.example.Messenger.Repository.UserRepository;
import com.example.Messenger.Utils.JwtTokenUtil;
import com.example.Messenger.Utils.KeyUtil;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.*;

@RestController
@CrossOrigin(
        origins = "http://localhost:8081",
        allowCredentials = "true"
)
public class ProfileController {
    private static final String ADMIN_CODE = "CHAU_XINH_DEP_2025";
    @Autowired
    private Cloudinary cloudinary;
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
    @GetMapping("/api/user/profile")
    public ResponseEntity<?> getUserProfile(
            @CookieValue(name = "token", required = false) String cookieToken,
            @RequestHeader(name = "Authorization", required = false) String headerToken
    ) {
        try {
            // 1️⃣ Ưu tiên token trong header, nếu không có thì lấy từ cookie
            String token = null;
            if (headerToken != null && headerToken.startsWith("Bearer ")) {
                token = headerToken.substring(7); // bỏ "Bearer "
            } else if (cookieToken != null) {
                token = cookieToken;
            }

            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Thiếu token, vui lòng đăng nhập"));
            }
            // 2️⃣ Giải mã token
            PublicKey publicKey = KeyUtil.loadOrCreateKeyPair().getPublic();
            Map<String, Object> userInfo = JwtTokenUtil.getUserFromToken(token, publicKey);
            System.out.println(userInfo.get("email"));
            Optional<User> user = Optional.of(userRepository.findUserByEmail((String) userInfo.get("email")).orElseThrow());

            UserProfileResponse response = new UserProfileResponse(user.get().getId(),user.get().getEmail(),user.get().getAvatar());
            System.out.println("test "  + response.getEmail());
            // 3️⃣ Trả về user info
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token không hợp lệ hoặc đã hết hạn"));
        }
    }
    @PostMapping("/verify/staff")
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
    @PostMapping("/user/avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        try {
            // 1️⃣ Lấy token từ header hoặc cookie
            String headerToken = request.getHeader("Authorization");
            String cookieToken = null;
            if (request.getCookies() != null) {
                cookieToken = java.util.Arrays.stream(request.getCookies())
                        .filter(c -> c.getName().equals("token"))
                        .findFirst()
                        .map(c -> c.getValue())
                        .orElse(null);
            }
            String token = null;
            if (headerToken != null && headerToken.startsWith("Bearer ")) {
                token = headerToken.substring(7);
            } else if (cookieToken != null) {
                token = cookieToken;
            }
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Thiếu token, vui lòng đăng nhập"));
            }
            // 2️⃣ Giải mã token → lấy email
            PublicKey publicKey = KeyUtil.loadOrCreateKeyPair().getPublic();
            Map<String, Object> userInfo = JwtTokenUtil.getUserFromToken(token, publicKey);
            String email = (String) userInfo.get("email");

            Optional<User> optionalUser = userRepository.findUserByEmail(email);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy user"));
            }

            User user = optionalUser.get();

            // 3️⃣ Kiểm tra file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File trống"));
            }

            // 4️⃣ Chuyển file thành chuỗi base64
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "avatars"
            ));

            // 🧾 Lấy URL ảnh
            String imageUrl = (String) uploadResult.get("secure_url");

            // 5️⃣ Lưu base64 vào DB
            user.setAvatar(imageUrl);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật avatar thành công",
                    "avatarBase64", imageUrl
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi tải lên avatar: " + e.getMessage()));
        }
    }

}
