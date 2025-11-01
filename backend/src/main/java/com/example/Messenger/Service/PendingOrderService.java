package com.example.Messenger.Service;

import com.example.Messenger.Record.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PendingOrderService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "order:pending:";
    @Autowired
    public PendingOrderService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void savePendingOrder(String token, OrderRequest request) {
        redisTemplate.opsForValue().set(PREFIX + token, request, 30, TimeUnit.MINUTES); // hết hạn sau 30 phút
    }

    public OrderRequest getPendingOrder(String token) {
        return (OrderRequest) redisTemplate.opsForValue().get(PREFIX + token);
    }

    public void deletePendingOrder(String token) {
        redisTemplate.delete(PREFIX + token);
    }
}