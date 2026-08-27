package com.aurora.blog.service;

import com.aurora.blog.web.ApiException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {
    private final StringRedisTemplate redis;

    public RateLimitService(StringRedisTemplate redis) { this.redis = redis; }

    public void check(String key, long max, Duration window) {
        Long count = redis.opsForValue().increment("aurora:rate:" + key);
        if (count != null && count == 1) redis.expire("aurora:rate:" + key, window);
        if (count != null && count > max) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "操作过于频繁，请稍后再试");
        }
    }
}
