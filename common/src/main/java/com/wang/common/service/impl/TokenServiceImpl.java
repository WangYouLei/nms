package com.wang.common.service.impl;

import com.wang.common.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 管理服务实现类
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private final StringRedisTemplate redisTemplate;

    // token 在 Redis 中的前缀
    private static final String TOKEN_PREFIX = "token:";
    // 默认过期时间：24小时
    private static final long DEFAULT_EXPIRE_SECONDS = 24 * 60 * 60;

    public TokenServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveToken(String key, String token, long expireSeconds) {
        String redisKey = TOKEN_PREFIX + key;
        try {
            redisTemplate.opsForValue().set(redisKey, token, expireSeconds, TimeUnit.SECONDS);
            log.info("Token 保存成功：key={}", redisKey);
        } catch (Exception e) {
            log.error("Token 保存失败：key={}, error={}", redisKey, e.getMessage());
        }
    }

    @Override
    public String getToken(String key) {
        String redisKey = TOKEN_PREFIX + key;
        try {
            return redisTemplate.opsForValue().get(redisKey);
        } catch (Exception e) {
            log.error("获取 Token 失败：key={}, error={}", redisKey, e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteToken(String key) {
        String redisKey = TOKEN_PREFIX + key;
        try {
            redisTemplate.delete(redisKey);
            log.info("Token 删除成功：key={}", redisKey);
        } catch (Exception e) {
            log.error("Token 删除失败：key={}, error={}", redisKey, e.getMessage());
        }
    }

    @Override
    public void deleteUserTokens(String role, Integer userId) {
        // 删除用户的所有 token（当前端的 token）
        String key = generateTokenKey(role, userId);
        deleteToken(key);
        log.info("删除用户 token：role={}, userId={}", role, userId);
    }

    @Override
    public boolean validateToken(String key, String token) {
        String redisKey = TOKEN_PREFIX + key;
        try {
            String storedToken = redisTemplate.opsForValue().get(redisKey);
            return token.equals(storedToken);
        } catch (Exception e) {
            log.error("Token 验证失败：key={}, error={}", redisKey, e.getMessage());
            return false;
        }
    }

    @Override
    public String generateTokenKey(String role, Integer userId) {
        // 格式：role:userId，如 "visitor:123"
        return role + ":" + userId;
    }
}
