package com.example.Messenger.Controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/user")
public class UserController {


    @GetMapping("/info")
    public ResponseEntity<?> userInfo(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("email", jwt.getClaimAsString("email"));
        response.put("username", jwt.getClaimAsString("username"));
        response.put("roles", jwt.getClaim("roles"));
        response.put("sub", jwt.getSubject());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/oauth2/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        // Lấy JSESSIONID từ cookie
        String jsessionId = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    jsessionId = cookie.getValue();
                }
            }
        }
        // Lấy token từ cookie (chị đã set ở successHandler)
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }
        // Lấy thông tin session
        HttpSession session = request.getSession(false);
        String sessionId = (session != null) ? session.getId() : null;
        // Trả về JSESSIONID, SESSION và token
        result.put("JSESSIONID", jsessionId);
        result.put("SESSION", sessionId);
        result.put("token", "Bearer "+token);

        return ResponseEntity.ok(result);
    }
}
