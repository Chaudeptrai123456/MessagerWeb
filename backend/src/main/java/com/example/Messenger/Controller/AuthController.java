package com.example.Messenger.Controller;

import com.example.Messenger.Service.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private RedisService redisService;

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // 🧹 Xóa cookie token
        Cookie tokenCookie = new Cookie("token", null);
        tokenCookie.setMaxAge(0);
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(true);
        response.addCookie(tokenCookie);

        // 🧹 Xóa refresh token trong Redis nếu có
        String accessToken = extractTokenFromCookie(request);
        if (accessToken != null) {
            redisService.deleteRefreshToken(accessToken);
            System.out.println("🧹 Refresh token deleted for access token: " + accessToken);
        }

        // ✅ Trả về JSON hoặc redirect
        return ResponseEntity.ok(Map.of("message", "Đã logout thành công"));
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
