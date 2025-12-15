package com.example.Messenger.Utils;

import org.springframework.core.io.ClassPathResource;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
public class KeyUtil {

    private static final String KEY_DIR = "keys"; // dùng tương đối -> Docker mount /app/keys
    private static final String PRIVATE_KEY_FILE = KEY_DIR + "/private.pem";
    private static final String PUBLIC_KEY_FILE = KEY_DIR + "/public.pem";

    /**
     * Tải keyPair từ file, nếu chưa có thì tự động tạo mới.
     */
    public static KeyPair loadOrCreateKeyPair() {
        try {
            Path privatePath = Paths.get(PRIVATE_KEY_FILE);
            Path publicPath = Paths.get(PUBLIC_KEY_FILE);

            // Nếu chưa có, tạo thư mục và key mới
            if (!Files.exists(privatePath) || !Files.exists(publicPath)) {
                System.out.println("⚠️ Không tìm thấy key -> tạo mới tại thư mục: " + KEY_DIR);
                Files.createDirectories(Paths.get(KEY_DIR));

                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);
                KeyPair keyPair = keyGen.generateKeyPair();

                saveKeyPair(keyPair, privatePath.toString(), publicPath.toString());
                return keyPair;
            }

            // Nếu có sẵn, đọc lại
            System.out.println("🔐 Đang load key từ file...");
            PrivateKey privateKey = readPrivateKey(privatePath);
            PublicKey publicKey = readPublicKey(publicPath);
            return new KeyPair(publicKey, privateKey);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể load hoặc tạo keyPair", e);
        }
    }

    /**
     * Lưu key pair ra file.
     */
    public static void saveKeyPair(KeyPair keyPair, String privatePath, String publicPath) throws IOException {
        try (FileWriter privWriter = new FileWriter(privatePath);
             FileWriter pubWriter = new FileWriter(publicPath)) {

            privWriter.write(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
            pubWriter.write(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        }
    }

    /**
     * Đọc private key từ file.
     */
    public static PrivateKey readPrivateKey(Path path) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(Files.readAllBytes(path));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /**
     * Đọc public key từ file.
     */
    public static PublicKey readPublicKey(Path path) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(Files.readAllBytes(path));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}