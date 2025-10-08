package com.example.Messenger.Utils;

import com.example.Messenger.Entity.User;
import com.example.Messenger.Entity.Authority;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class JwtTokenUtil {
    private KeyUtil keyUtil;
    public static String generateToken(User user, PrivateKey privateKey) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plus(1, ChronoUnit.HOURS);

            // Lấy danh sách role từ user
            List<String> roles = user.getAuthorities()
                    .stream()
                    .map(Authority::getName)
                    .toList();

            // Tạo claims
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .claim("email", user.getEmail())
                    .claim("username", user.getUsername())
                    .claim("roles", roles)
                    .issuer("http://localhost:9999")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .build();

            // Tạo header RS256
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            // Ký token bằng private key
            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new RSASSASigner(privateKey);
            signedJWT.sign(signer);

            String token = signedJWT.serialize();
            System.out.println("✅ Token đã được ký bằng RS256: " + token);
            return token;

        } catch (Exception e) {
            System.err.println("  Lỗi khi tạo JWT: " + e.getMessage());
            throw new RuntimeException("Không thể tạo JWT", e);
        }
    }
    public static String generateTokenRefresh(User user, PrivateKey privateKey) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plus(7, ChronoUnit.DAYS);

            // Lấy danh sách role từ user
            List<String> roles = user.getAuthorities()
                    .stream()
                    .map(Authority::getName)
                    .toList();

            // Tạo claims
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .claim("email", user.getEmail())
                    .claim("username", user.getUsername())
                    .claim("roles", roles)
                    .issuer("http://localhost:9999")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .build();

            // Tạo header RS256
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            // Ký token bằng private key
            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new RSASSASigner(privateKey);
            signedJWT.sign(signer);

            String token = signedJWT.serialize();
            System.out.println("✅ Token đã được ký bằng RS256: " + token);
            return token;

        } catch (Exception e) {
            System.err.println("  Lỗi khi tạo JWT: " + e.getMessage());
            throw new RuntimeException("Không thể tạo JWT", e);
        }
    }
    public static String verifyAndGenerateNewAccessToken(String refreshToken) {
        try {
            // Đọc public key từ file
            PublicKey publicKey = KeyUtil.loadPublicKey("keys/public.pem");

            // Đọc private key từ file
            PrivateKey privateKey = KeyUtil.loadPrivateKey("keys/private.pem");
            SignedJWT signedJWT = SignedJWT.parse(refreshToken);
            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);

            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException(" Refresh token không hợp lệ");
            }

            // Kiểm tra hạn sử dụng
            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration.before(new Date())) {
                throw new RuntimeException("  Refresh token đã hết hạn");
            }

            // Lấy claims từ refresh token
            JWTClaimsSet oldClaims = signedJWT.getJWTClaimsSet();
            Instant now = Instant.now();
            Instant newExpiry = now.plus(1, ChronoUnit.HOURS); // Access token sống 1 tiếng

            // Tạo claims mới cho access token
            JWTClaimsSet newClaims = new JWTClaimsSet.Builder()
                    .subject(oldClaims.getSubject())
                    .claim("email", oldClaims.getStringClaim("email"))
                    .claim("username", oldClaims.getStringClaim("username"))
                    .claim("roles", oldClaims.getClaim("roles"))
                    .issuer("http://localhost:9999")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(newExpiry))
                    .build();

            // Tạo header và ký token
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT newSignedJWT = new SignedJWT(header, newClaims);
            JWSSigner signer = new RSASSASigner(privateKey);
            newSignedJWT.sign(signer);

            return newSignedJWT.serialize();
        } catch (Exception e) {
            System.err.println("  Lỗi khi xác thực refresh token: " + e.getMessage());
            throw new RuntimeException("Không thể tạo access token mới", e);
        }
    }

}
