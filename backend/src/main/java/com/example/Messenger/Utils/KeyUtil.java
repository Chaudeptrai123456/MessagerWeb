package com.example.Messenger.Utils;

import org.springframework.core.io.ClassPathResource;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyUtil {

    public static void saveKeyPair(KeyPair keyPair, String privatePath, String publicPath) throws IOException {
        // Lưu private key
        try (FileWriter writer = new FileWriter(privatePath)) {
            writer.write(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        }

        // Lưu public key
        try (FileWriter writer = new FileWriter(publicPath)) {
            writer.write(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        }
    }

    public static KeyPair loadKeyPair(String privatePath, String publicPath) throws Exception {
        byte[] privateBytes = Files.readAllBytes(Paths.get(privatePath));
        byte[] publicBytes = Files.readAllBytes(Paths.get(publicPath));

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateBytes)));

        PublicKey publicKey = keyFactory.generatePublic(
                new X509EncodedKeySpec(Base64.getDecoder().decode(publicBytes)));

        return new KeyPair(publicKey, privateKey);
    }
    public static PrivateKey loadPrivateKey(String path) throws Exception {
        InputStream inputStream = new ClassPathResource(path).getInputStream();
        String key = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static PublicKey loadPublicKey(String path) throws Exception {
        InputStream inputStream = new ClassPathResource(path).getInputStream();
        String key = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
