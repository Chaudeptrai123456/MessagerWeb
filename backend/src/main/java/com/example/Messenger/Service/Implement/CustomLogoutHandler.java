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
        if (jwtToken != null) {
            redisService.deleteRefreshToken(jwtToken);
        }
        deleteCookie(response, "token");
        deleteCookie(response, "refresh_token");
        deleteCookie(response, "JSESSIONID");
        SecurityContextHolder.clearContext();
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

