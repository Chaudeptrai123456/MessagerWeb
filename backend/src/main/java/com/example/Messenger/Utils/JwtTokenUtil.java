package com.example.Messenger.Utils;

import com.example.Messenger.Entity.User;
import com.example.Messenger.Entity.Authority;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.*;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtTokenUtil {

    // ✅ Sinh Access Token
    public static String generateToken(User user, PrivateKey privateKey) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plus(1, ChronoUnit.HOURS);

            List<String> roles = user.getAuthorities()
                    .stream()
                    .map(Authority::getName)
                    .toList();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .claim("email", user.getEmail())
                    .claim("username", user.getUsername())
                    .claim("avatar",user.getAvatar())
                    .claim("roles", roles)
                    .issuer("http://localhost:9999")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(),
                    claims
            );

            signedJWT.sign(new RSASSASigner(privateKey));

            String token = signedJWT.serialize();
            System.out.println("✅ JWT Access Token tạo thành công!");
            return token;

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo Access Token: " + e.getMessage());
            throw new RuntimeException("Không thể tạo JWT access token", e);
        }
    }

    // ✅ Sinh Refresh Token
    public static String generateTokenRefresh(User user, PrivateKey privateKey) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plus(7, ChronoUnit.DAYS);

            List<String> roles = user.getAuthorities()
                    .stream()
                    .map(Authority::getName)
                    .toList();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .claim("email", user.getEmail())
                    .claim("username", user.getUsername())
                    .claim("roles", roles)
                    .issuer("http://localhost:9999")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(),
                    claims
            );

            signedJWT.sign(new RSASSASigner(privateKey));

            String token = signedJWT.serialize();
            System.out.println("🔁 Refresh Token tạo thành công!");
            return token;

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo Refresh Token: " + e.getMessage());
            throw new RuntimeException("Không thể tạo JWT refresh token", e);
        }
    }

    // ✅ Xác thực Refresh Token và sinh Access Token mới
    public static String verifyAndGenerateNewAccessToken(String refreshToken) {
        try {
            // 🔐 Lấy key pair (tự tạo nếu chưa có)
            KeyPair keyPair = KeyUtil.loadOrCreateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            SignedJWT signedJWT = SignedJWT.parse(refreshToken);
            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);

            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Refresh token không hợp lệ");
            }

            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration.before(new Date())) {
                throw new RuntimeException("Refresh token đã hết hạn");
            }

            JWTClaimsSet oldClaims = signedJWT.getJWTClaimsSet();
            Instant now = Instant.now();
            Instant newExpiry = now.plus(1, ChronoUnit.HOURS);

            JWTClaimsSet newClaims = new JWTClaimsSet.Builder()
                    .subject(oldClaims.getSubject())
                    .claim("email", oldClaims.getStringClaim("email"))
                    .claim("username", oldClaims.getStringClaim("username"))
                    .claim("roles", oldClaims.getClaim("roles"))
                    .issuer("http://localhost:9999")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(newExpiry))
                    .build();

            SignedJWT newSignedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).build(),
                    newClaims
            );

            newSignedJWT.sign(new RSASSASigner(privateKey));

            System.out.println("✅ Access Token mới được tạo thành công!");
            return newSignedJWT.serialize();

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xác thực Refresh Token: " + e.getMessage());
            throw new RuntimeException("Không thể tạo access token mới", e);
        }
    }
    // ✅ Giải mã token để lấy thông tin user
    public static Map<String, Object> getUserFromToken(String token, PublicKey publicKey) {
        try {
            // 1️⃣ Parse token
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 2️⃣ Verify chữ ký để đảm bảo token hợp lệ
            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("Token không hợp lệ (chữ ký sai)");
            }

            // 3️⃣ Lấy phần claims (payload)
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // 4️⃣ Kiểm tra hạn sử dụng
            Date expiration = claims.getExpirationTime();
            if (expiration.before(new Date())) {
                throw new RuntimeException("Token đã hết hạn");
            }

            // 5️⃣ Lấy thông tin user từ claims
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("email", claims.getStringClaim("email"));
            userProfile.put("username", claims.getStringClaim("username"));
            userProfile.put("roles", claims.getClaim("roles"));
            userProfile.put("issuedAt", claims.getIssueTime());
            userProfile.put("expiresAt", claims.getExpirationTime());

            System.out.println("👤 Giải mã token thành công! " + userProfile);
            return userProfile;

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi đọc token: " + e.getMessage());
            throw new RuntimeException("Không thể giải mã JWT token", e);
        }
    }

}
