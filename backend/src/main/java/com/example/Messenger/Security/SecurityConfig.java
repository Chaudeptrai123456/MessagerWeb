package com.example.Messenger.Security;

import com.example.Messenger.Entity.User;
import com.example.Messenger.Service.UserService;
import com.example.Messenger.Utils.JwtTokenUtil;
import com.example.Messenger.Utils.KeyUtil;
import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.*;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Optional;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final KeyPair keyPair;
    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
        this.keyPair = generateOrLoadKeyPair();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error", "/default-ui.css").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                            String email = oauthUser.getAttribute("email");
                            String name = oauthUser.getAttribute("name");

                            User user = userService.handleLogin(email, name);
                            String jwtToken = JwtTokenUtil.generateToken(user, keyPair.getPrivate());

                            // Gắn token vào Cookie
                            Cookie cookie = new Cookie("token", jwtToken);
                            cookie.setHttpOnly(true);
                            cookie.setSecure(true); // dùng HTTPS thì true
                            cookie.setPath("/");
                            cookie.setMaxAge((int) Duration.ofHours(1).toSeconds());
                            response.addCookie(cookie);

                            // Redirect sang /auth/test
                            String redirectUrl = Optional.ofNullable(
                                    new HttpSessionRequestCache().getRequest(request, response)
                            ).map(SavedRequest::getRedirectUrl).orElse("/auth/test");

                            response.sendRedirect(redirectUrl);
                        })
                )                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwkSetUri("http://localhost:9999/oauth2/jwks")));

        return http.build();
    }

    private KeyPair generateOrLoadKeyPair() {
        try {
            Path privatePath = Paths.get("src/main/resources/keys/private.pem");
            Path publicPath = Paths.get("src/main/resources/keys/public.pem");

            if (Files.exists(privatePath) && Files.exists(publicPath)) {
                return KeyUtil.loadKeyPair(privatePath.toString(), publicPath.toString());
            } else {
                KeyPair generated = generateRsaKeyPair();
                KeyUtil.saveKeyPair(generated, privatePath.toString(), publicPath.toString());
                return generated;
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể load hoặc tạo keyPair", e);
        }
    }

    private KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tạo RSA key", e);
        }
    }

//    @Bean
//    public JwtDecoder jwtDecoder() {
//        return NimbusJwtDecoder.withJwkSetUri("http://localhost:9999/oauth2/jwks").build();
//    }
@Bean
public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
}

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtDecoder jwtDecoder) {
        return new JwtAuthenticationFilter(jwtDecoder);
    }
}


//@Configuration
//public class SecurityConfig {
//
//    private final KeyPair keyPair;
//    private final UserService userService;
//
//    public SecurityConfig(UserService userService) {
//        this.userService = userService;
//        try {
//            Path privatePath = Paths.get("src/main/resources/keys/private.pem");
//            Path publicPath = Paths.get("src/main/resources/keys/public.pem");
//
//            if (Files.exists(privatePath) && Files.exists(publicPath)) {
//                this.keyPair = KeyUtil.loadKeyPair(privatePath.toString(), publicPath.toString());
//            } else {
//                KeyPair generated = generateRsaKeyPair();
//                KeyUtil.saveKeyPair(generated, privatePath.toString(), publicPath.toString());
//                this.keyPair = generated;
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Không thể load hoặc tạo keyPair", e);
//        }
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http,
//                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
//        http
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/", "/login", "/error","/default-ui.css").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .oauth2Login(oauth2 -> oauth2.successHandler((request, response, authentication) -> {
//                    OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
//                    String email = oauthUser.getAttribute("email");
//                    String name = oauthUser.getAttribute("name");
//                    String avatar = oauthUser.getAttribute("picture");
//
//                    try {
//                        User user = this.userService.handleLogin(email, name);
//                        String jwtToken = JwtTokenUtil.generateToken(user, keyPair.getPrivate());
//
//                        Cookie cookie = new Cookie("token", jwtToken);
//                        cookie.setHttpOnly(true);
//                        cookie.setSecure(false);
//                        cookie.setPath("/");
//                        cookie.setMaxAge((int) Duration.ofHours(1).toSeconds());
//
//                        response.addCookie(cookie);
//
//                        // ✅ Không redirect, chỉ trả về 200 OK
//                        response.setStatus(HttpServletResponse.SC_OK);
////                        response.getWriter().write("Login successful. JWT issued.");
//                        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
//                        String redirectUrl = (savedRequest != null)
//                                ? savedRequest.getRedirectUrl()
//                                : "/auth/test"; // nếu không có thì về home
//
//                        response.sendRedirect(redirectUrl);
//                        response.flushBuffer();
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }))
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt.jwkSetUri("http://localhost:9999/oauth2/jwks"))
//                );
//
//        return http.build();
//    }
//
//    // 👉 JWK Set cho Authorization Server
//    @Bean
//    public JWKSet jwkSet() {
//        return new JWKSet(generateRsaKey());
//    }
//
//    @Bean
//    public JWKSource<SecurityContext> jwkSource() {
//        RSAKey rsaKey = generateRsaKey();
//        JWKSet jwkSet = new JWKSet(rsaKey);
//        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
//    }
//
//    @Bean
//    public OAuth2AuthorizationService authorizationService(RegisteredClientRepository registeredClientRepository) {
//        return new InMemoryOAuth2AuthorizationService();
//    }
//
//    @Bean
//    public OAuth2AuthorizationConsentService authorizationConsentService(RegisteredClientRepository registeredClientRepository) {
//        return new InMemoryOAuth2AuthorizationConsentService();
//    }
//
//    private RSAKey generateRsaKey() {
//        KeyPair keyPair = generateRsaKeyPair();
//        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
//                .privateKey((RSAPrivateKey) keyPair.getPrivate())
//                .keyID(UUID.randomUUID().toString())
//                .build();
//    }
//
//    @Bean
//    public JwtDecoder jwtDecoder() {
//        String jwkSetUri = "http://localhost:9999/oauth2/jwks";
//        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
//    }
//
//
//    private KeyPair generateRsaKeyPair() {
//        try {
//            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
//            generator.initialize(2048);
//            return generator.generateKeyPair();
//        } catch (Exception e) {
//            throw new IllegalStateException("Không thể tạo RSA key", e);
//        }
//    }
//
//    @Bean
//    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtDecoder jwtDecoder) {
//        return new JwtAuthenticationFilter(jwtDecoder);
//    }
//}