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

        String jwtToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }
        if (jwtToken != null) {
            redisService.deleteRefreshToken(jwtToken);
        }
        SecurityContextHolder.clearContext();
        Cookie deleteCookie = new Cookie("token", null);
        deleteCookie.setHttpOnly(false);
        deleteCookie.setSecure(false); // true nếu HTTPS
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        response.addCookie(deleteCookie);
    }
}
