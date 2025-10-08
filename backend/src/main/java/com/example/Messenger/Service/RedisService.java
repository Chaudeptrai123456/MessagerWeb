package com.example.Messenger.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
@Service
public class RedisService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private final ValueOperations<String, String> valueOps;
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.valueOps = redisTemplate.opsForValue();
        this.objectMapper = objectMapper;
    }

    // ====== Token methods ======
    public void saveRefreshToken(String token, String refreshToken) {
        valueOps.set(buildKey(token), refreshToken, REFRESH_TOKEN_TTL);
    }

    public String getRefreshToken(String token) {
        return valueOps.get(buildKey(token));
    }

    public void deleteRefreshToken(String token) {
        valueOps.getOperations().delete(buildKey(token));
    }

    private String buildKey(String userId) {
        return "refresh:user:" + userId;
    }

    // ====== Generic cache methods ======
    public <T> void save(String key, T value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            valueOps.set(key, json, ttl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize cache data", e);
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = valueOps.get(key);
            if (json == null) return null;
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize cache data", e);
        }
    }

    public <T> T getList(String key, Class<T> clazz) {
        try {
            String json = valueOps.get(key);
            return json != null ? objectMapper.readValue(json, clazz) : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize cache data", e);
        }
    }
    public <T> void saveList(String key, T data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            valueOps.set(key, json, CACHE_TTL);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize cache data", e);
        }
    }
    public void delete(String key) {
        valueOps.getOperations().delete(key);
    }
}
