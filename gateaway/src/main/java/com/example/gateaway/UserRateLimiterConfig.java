package com.example.gateaway;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class UserRateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Lấy JWT từ header Authorization
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // Nếu không có token → coi như 1 user chung
                return Mono.just("anonymous");
            }

            String token = authHeader.substring(7);

            // ✅ Tách JWT claim "sub" (userId)
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return Mono.just("anonymous");
            }

            try {
                String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                String userId = payloadJson.replaceAll(".*\"sub\":\"([^\"]+)\".*", "$1");

                return Mono.just(userId);
            } catch (Exception e) {
                return Mono.just("anonymous");
            }
        };
    }
}