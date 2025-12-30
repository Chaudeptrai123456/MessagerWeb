package com.example.Messenger.Security;

import com.example.Messenger.Entity.User;
import com.example.Messenger.Service.Implement.CustomLogoutHandler;
import com.example.Messenger.Service.Implement.CustomLogoutSuccessHandler;
import com.example.Messenger.Service.Implement.UserService;
import com.example.Messenger.Service.RedisService;
import com.example.Messenger.Utils.JwtTokenUtil;
import com.example.Messenger.Utils.KeyUtil;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.cors.CorsConfiguration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;
    private final RedisService redisService;
    private final CustomLogoutHandler customLogoutHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    @Value("frontend-url")
    private String url;

    public SecurityConfig(UserService userService, RedisService redisService, CustomLogoutHandler customLogoutHandler, CustomLogoutSuccessHandler customLogoutSuccessHandler) {
        this.userService = userService;
        this.redisService = redisService;
        this.customLogoutHandler = customLogoutHandler;
        this.customLogoutSuccessHandler = customLogoutSuccessHandler;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   KeyPair keyPair) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setCreateSessionAllowed(true);
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,"/api/orders/confirm","/","/logout").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/categories").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/products/top-discount").permitAll()
                        .requestMatchers(
                                "/",
                                "/login",
                                "/error",
                                "/default-ui.css",
                                "/.well-known/appspecific/com.chrome.devtools.json",
                                "/api/products/get",
                                "/api/products/search/**"
                        ).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers("/api/orders").authenticated()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("token")
                )

                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                            String email = oauthUser.getAttribute("email");
                            String name = oauthUser.getAttribute("name");
                            String picture = oauthUser.getAttribute("picture");
                            User user = userService.handleLogin(email, name,picture);
                            String jwtToken = JwtTokenUtil.generateToken(user, keyPair.getPrivate());
                            String refreshToken = JwtTokenUtil.generateTokenRefresh(user, keyPair.getPrivate());
                            redisService.saveRefreshToken(jwtToken, refreshToken);

                            Cookie cookie = new Cookie("token", jwtToken);
                            cookie.setHttpOnly(false);
                            cookie.setSecure(false); // ⚠️ Nếu đang test ở localhost thì để false
                            cookie.setPath("/");
                            cookie.setMaxAge((int) Duration.ofHours(1).toSeconds());
                            cookie.setAttribute("SameSite", "Lax"); // hoặc "None" nếu cần
                            response.addCookie(cookie);
                            response.sendRedirect("/api/user/oauth2/info");

                            // // Gửi refresh token qua header (cookie không chứa được 2 key)
                            // SavedRequest savedRequest = requestCache.getRequest(request, response);
                            // if (savedRequest != null) {
                            //     System.out.println("🔹 Saved redirect: " + savedRequest.getRedirectUrl());
                            // } else {
                            //     System.out.println("⚠️ No saved request found!");
                            // }
                            // String redirectUrl;
                            // if (savedRequest != null) {
                            //     redirectUrl = savedRequest.getRedirectUrl();
                            //     // Xóa saved request để tránh bị redirect lặp
                            //     requestCache.removeRequest(request, response);
                            // } else {
                            //     redirectUrl = "http://localhost:3000"; // fallback mặc định
                            // }
                            // // Redirect tới URL cũ hoặc fallback
                            // response.sendRedirect(redirectUrl);
                        })
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder(keyPair))
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = KeyUtil.loadOrCreateKeyPair();

        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID("auth-key") // ❗ CỐ ĐỊNH
                .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }
    // ✅ Load hoặc tạo KeyPair (sử dụng KeyUtil)
    @Bean
    public KeyPair keyPair() {
        return KeyUtil.loadOrCreateKeyPair();
    }
    @Bean
    public JwtDecoder jwtDecoder(KeyPair keyPair) {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtDecoder jwtDecoder) {
        return new JwtAuthenticationFilter(jwtDecoder);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}