package com.example.Messenger.Service.Implement;

import com.example.Messenger.Service.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final RedisService redisService;

    public CustomLogoutHandler(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {

        // 1️⃣ Lấy token từ cookie
        String jwtToken = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("token".equals(c.getName())) {
                    jwtToken = c.getValue();
                    break;
                }
            }
        }

        // 2️⃣ Xoá refresh token trong Redis
        if (jwtToken != null) {
            redisService.deleteRefreshToken(jwtToken);
        }

        // 3️⃣ Xoá cookie token
        deleteCookie(response, "token");

        // 4️⃣ Xoá refresh cookie nếu có
        deleteCookie(response, "refresh_token");

        // 5️⃣ Xoá JSESSIONID (Spring Session)
        deleteCookie(response, "JSESSIONID");

        // 6️⃣ Clear Security Context
        SecurityContextHolder.clearContext();
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true nếu HTTPS
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

