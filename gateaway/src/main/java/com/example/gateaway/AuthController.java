package com.example.gateaway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    @GetMapping("/login/oauth2/code/messenger")
    public String handleOAuth2Callback() {
        return "test "; // hoặc xử lý token nếu cần
    }
    @RequestMapping("/error")
    public String handleError() {
        return "error"; // hoặc trả về JSON tùy app
    }
}
    