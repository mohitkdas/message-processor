package com.mohit.message.processor.service;

import com.mohit.message.processor.model.ChatMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private final RedisTemplate<String, ChatMessage> redisTemplate;

    public CacheService(RedisTemplate<String, ChatMessage> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheMessage(String roomId, ChatMessage message) {
        String key = "chat:room:" + roomId;
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
        if (redisTemplate.opsForList().size(key) > 50) {
            redisTemplate.opsForList().leftPop(key);
        }
    }
}
