package com.example.Messenger.Controller;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/auth/test")
    public String test(@CookieValue(name = "token", required = false) String token) {
        if (token == null) {
            return "No token found!";
        }
        return "Your token: " + token;
    }
}
