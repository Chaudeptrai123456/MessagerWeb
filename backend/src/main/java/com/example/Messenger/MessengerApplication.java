package com.example.Messenger;
import com.example.Messenger.Utils.KeyUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

@SpringBootApplication
@EnableCaching
public class MessengerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessengerApplication.class, args);
		try {
			// Tạo thư mục keys nếu chưa có
			Path keyDir = Paths.get("keys");
			if (!Files.exists(keyDir)) {
				Files.createDirectories(keyDir);
			}

			// Tạo key pair
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			KeyPair keyPair = keyGen.generateKeyPair();

			// Lưu key vào file
			String privatePath = "keys/private.key";
			String publicPath = "keys/public.key";
			KeyUtil.saveKeyPair(keyPair, privatePath, publicPath);

			System.out.println("✅ Đã tạo và lưu key vào thư mục 'keys'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
