package com.example.Messenger.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisTestRunner implements CommandLineRunner {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void run(String... args) {
        // Ghi dữ liệu vào Redis
        redisTemplate.opsForValue().set("test-key", "Hello Redis!");

        // Đọc dữ liệu từ Redis
        String value = redisTemplate.opsForValue().get("test-key");

        System.out.println("✅ Redis value: " + value);
    }
}
