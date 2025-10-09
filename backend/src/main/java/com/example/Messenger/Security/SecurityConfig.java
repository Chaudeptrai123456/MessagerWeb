package com.example.Messenger.Security;

import com.example.Messenger.Entity.User;
import com.example.Messenger.Service.Implement.UserService;
import com.example.Messenger.Service.RedisService;
import com.example.Messenger.Utils.JwtTokenUtil;
import com.example.Messenger.Utils.KeyUtil;
import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserService userService;
    private final RedisService redisService;
    public SecurityConfig(UserService userService, RedisService redisService) {
        this.userService = userService;
        this.redisService = redisService;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   KeyPair keyPair) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/products/search/**","/", "/login", "/api/products/get","/error", "/default-ui.css", "/.well-known/appspecific/com.chrome.devtools.json").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                            String email = oauthUser.getAttribute("email");
                            String name = oauthUser.getAttribute("name");
                            User user = userService.handleLogin(email, name);
                            String jwtToken = JwtTokenUtil.generateToken(user, keyPair.getPrivate());
                            String refresherToken = JwtTokenUtil.generateTokenRefresh(user, keyPair.getPrivate());
                            redisService.saveRefreshToken(jwtToken, refresherToken);
                            Cookie cookie = new Cookie("token", jwtToken);
                            cookie.setAttribute("refreshToken",refresherToken);
                            cookie.setHttpOnly(true);
                            cookie.setSecure(true);
                            cookie.setPath("/");
                            cookie.setMaxAge((int) Duration.ofHours(1).toSeconds());
                            response.addCookie(cookie);
                            String redirectUrl = Optional.ofNullable(
                                    new HttpSessionRequestCache().getRequest(request, response)
                            ).map(SavedRequest::getRedirectUrl).orElse("/auth/test");
                            response.sendRedirect(redirectUrl);
                        })
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder(keyPair))
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder(keyPair))));

        return http.build();
    }

    @Bean
    public KeyPair keyPair() {
        try {
            Path privatePath = Paths.get("src/main/resources/keys/private.pem");
            Path publicPath = Paths.get("src/main/resources/keys/public.pem");

            if (Files.exists(privatePath) && Files.exists(publicPath)) {
                return KeyUtil.loadKeyPair(privatePath.toString(), publicPath.toString());
            } else {
                KeyPair generated = generateRsaKeyPair();
                KeyUtil.saveKeyPair(generated, privatePath.toString(), publicPath.toString());

                RSAPublicKey pub = (RSAPublicKey) generated.getPublic();
                RSAPrivateKey priv = (RSAPrivateKey) generated.getPrivate();

                System.out.println("🔍 Public modulus: " + pub.getModulus());
                System.out.println("🔍 Private modulus: " + priv.getModulus());

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
        grantedAuthoritiesConverter.setAuthorityPrefix(""); // hoặc "ROLE_" nếu claim không có prefix

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
