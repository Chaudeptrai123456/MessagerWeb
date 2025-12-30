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

    private static final Path KEY_DIR = Paths.get("/app/keys"); // ❗ tuyệt đối cho Docker
    private static final Path PRIVATE_KEY_FILE = KEY_DIR.resolve("private.pem");
    private static final Path PUBLIC_KEY_FILE = KEY_DIR.resolve("public.pem");

    public static KeyPair loadOrCreateKeyPair() {
        try {
            if (Files.exists(PRIVATE_KEY_FILE) && Files.exists(PUBLIC_KEY_FILE)) {
                System.out.println("🔐 Load RSA key from PEM files");
                return new KeyPair(readPublicKey(), readPrivateKey());
            }

            System.out.println("⚠️ RSA key not found → generating new one");
            Files.createDirectories(KEY_DIR);

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            writePem(PRIVATE_KEY_FILE, "PRIVATE KEY", keyPair.getPrivate().getEncoded());
            writePem(PUBLIC_KEY_FILE, "PUBLIC KEY", keyPair.getPublic().getEncoded());

            return keyPair;

        } catch (Exception e) {
            throw new IllegalStateException("Cannot load or create RSA key pair", e);
        }
    }

    /* ================= PEM ================= */

    private static void writePem(Path path, String type, byte[] content) throws IOException {
        String pem = "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(content)
                + "\n-----END " + type + "-----\n";
        Files.writeString(path, pem);
    }

    private static byte[] readPem(Path path) throws IOException {
        String pem = Files.readString(path);
        return Base64.getDecoder().decode(
                pem.replaceAll("-----BEGIN (.*)-----", "")
                        .replaceAll("-----END (.*)-----", "")
                        .replaceAll("\\s", "")
        );
    }

    private static PrivateKey readPrivateKey() throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(readPem(PRIVATE_KEY_FILE));
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static PublicKey readPublicKey() throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(readPem(PUBLIC_KEY_FILE));
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
