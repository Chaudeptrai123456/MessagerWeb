package com.example.Messenger.Utils;

import com.example.Messenger.Entity.User;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.example.Messenger.Entity.Authority;
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

            // Lấy danh sách authority từ User
            List<String> roles = user.getAuthorities()
                    .stream()
                    .map(Authority::getName)   // giả sử class Authority có field name
                    .toList();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail()) // subject là email
                    .claim("email", user.getEmail())
                    .claim("username", user.getUsername())
                    .claim("roles", roles)            // add roles vào JWT
                    .issuer("http://localhost:8080")  // issuer của em
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);

            // Sign với private key
            JWSSigner signer = new RSASSASigner(privateKey);
            signedJWT.sign(signer);

            return signedJWT.serialize();

        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo JWT", e);
        }
    }
}
