package com.example.annonymus_v1.ServiceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final long LIKE_EXPIRATION_MINUTES = 1;
    private final RedisTemplate<String, String> redisTemplate;

    public boolean allowAction(UUID postId, String clientIdentifier) {
        String key = "action:" + postId + ":" + clientIdentifier;

        Boolean result = redisTemplate.opsForValue().setIfAbsent(
                key,
                "1",
                LIKE_EXPIRATION_MINUTES,
                TimeUnit.MINUTES
        );
        return Boolean.TRUE.equals(result);
    }
}
