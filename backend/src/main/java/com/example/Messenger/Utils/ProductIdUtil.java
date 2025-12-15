package com.example.Messenger.Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProductIdUtil {
    public static String generateId(String name, float[] embedding) {
        try {
            // slug tên (chỉ giữ chữ + số)
            String slug = name.toLowerCase()
                    .replaceAll("[^a-z0-9]", "");
            if (slug.length() > 8) slug = slug.substring(0, 8); // cắt ngắn cho gọn

            // ngày tạo
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd

            // hash embedding → lấy 2 byte đầu
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(floatArrayToBytes(embedding));
            String shortHash = String.format("%02x%02x", hash[0], hash[1]);

            return slug + "-" + date + "-" + shortHash;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] floatArrayToBytes(float[] arr) {
        byte[] bytes = new byte[arr.length * 4];
        for (int i = 0; i < arr.length; i++) {
            int intBits = Float.floatToIntBits(arr[i]);
            bytes[i * 4] = (byte) (intBits >> 24);
            bytes[i * 4 + 1] = (byte) (intBits >> 16);
            bytes[i * 4 + 2] = (byte) (intBits >> 8);
            bytes[i * 4 + 3] = (byte) (intBits);
        }
        return bytes;
    }
}
