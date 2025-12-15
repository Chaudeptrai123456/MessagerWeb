package com.example.Messenger.Security;

import com.example.Messenger.Service.RedisService;
import com.example.Messenger.Utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private RedisService redisService;
    private final JwtDecoder jwtDecoder;
    private JwtTokenUtil jwtTokenUtil;
    public JwtAuthenticationFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
         if (token != null) {
            try {
                Jwt jwt = jwtDecoder.decode(token);
                Instant expiresAt = jwt.getExpiresAt();
                Instant now = Instant.now();
                if (expiresAt!=null && expiresAt.isBefore(now)) {
//                  if (expiresAt == null || expiresAt.isAfter(now)) {
                    var refreshToken = redisService.getRefreshToken("refresh:user:"+token);
                    if (refreshToken != null) {
                        var accessToken = jwtTokenUtil.verifyAndGenerateNewAccessToken(token);
                        Jwt jwtRefresh = jwtDecoder.decode(refreshToken);
                        String username = jwtRefresh.getClaimAsString("username");
                        List<String> roles = jwtRefresh.getClaimAsStringList("roles");
                        if (roles == null) roles = List.of();
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .toList();
                        Cookie cookie = new Cookie("token",accessToken);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
                String username = jwt.getClaimAsString("username");
                List<String> roles = jwt.getClaimAsStringList("roles");
                if (roles == null) roles = List.of();
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                System.err.println("JWT decode failed: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
    private String extractAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/.well-known/jwks.json")
                || path.startsWith("/error")
                || path.startsWith("/favicon.ico")
                || path.startsWith("/login");
    }
}
