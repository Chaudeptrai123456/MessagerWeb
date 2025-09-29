package com.example.Messenger.Utils;

import com.example.Messenger.Entity.User;
import com.example.Messenger.Entity.Authority;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.*;

import java.security.PrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class JwtTokenUtil {

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
            System.err.println("❌ Lỗi khi tạo JWT: " + e.getMessage());
            throw new RuntimeException("Không thể tạo JWT", e);
        }
    }

}
