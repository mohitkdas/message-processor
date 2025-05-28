package com.mohit.message.processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohit.message.processor.model.ChatMessage;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final RedisAdvancedClusterCommands<String, String> redisCommands;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CacheService(RedisAdvancedClusterCommands<String, String> redisCommands) {
        this.redisCommands = redisCommands;
    }

    public void cacheMessage(String roomId, ChatMessage message) {
        try {
            String key = "chat:room:" + roomId;
            String json = objectMapper.writeValueAsString(message);

            redisCommands.rpush(key, json);
            if (redisCommands.llen(key) > 50) {
                redisCommands.lpop(key);
            }

        } catch (Exception e) {
            throw new RuntimeException("Redis caching failed", e);
        }
    }
}
