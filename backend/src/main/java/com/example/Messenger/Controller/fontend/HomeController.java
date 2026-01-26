package com.example.Messenger.Controller.fontend;

import com.example.Messenger.Service.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller("/")
public class HomeController {
    private final RedisService redisService;
    @Autowired
    public HomeController(RedisService redisService) {
        this.redisService = redisService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    @GetMapping("/logout/page")
    public String logoutPage() {
        return "logout";
    }
    @PostMapping("/logout")
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
        cookie.setSecure(false); // true nếu HTTPS
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}