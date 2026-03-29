package com.wang.common.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wang.common.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 通用缓存服务实现类
 */
@Slf4j
@Service
public class CacheServiceImpl implements CacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 默认过期时间：30分钟
    private static final long DEFAULT_TTL_SECONDS = 1800L;

    public CacheServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        // 注册 JavaTimeModule 以支持 Java 8 日期时间类型
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Object get(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return objectMapper.readValue(value, Object.class);
        } catch (Exception e) {
            log.error("获取缓存失败：key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return objectMapper.readValue(value, clazz);
        } catch (Exception e) {
            log.error("获取缓存失败：key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void set(String key, Object value, long ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
            log.debug("缓存设置成功：key={}, ttl={}秒", key, ttlSeconds);
        } catch (JsonProcessingException e) {
            log.error("缓存序列化失败：key={}, error={}", key, e.getMessage());
        } catch (Exception e) {
            log.error("设置缓存失败：key={}, error={}", key, e.getMessage());
        }
    }

    @Override
    public void set(String key, Object value) {
        set(key, value, DEFAULT_TTL_SECONDS);
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("缓存删除成功：key={}", key);
        } catch (Exception e) {
            log.error("删除缓存失败：key={}, error={}", key, e.getMessage());
        }
    }

    @Override
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("批量删除缓存成功：pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("批量删除缓存失败：pattern={}, error={}", pattern, e.getMessage());
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查缓存是否存在失败：key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public void expire(String key, long ttlSeconds) {
        try {
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("设置过期时间失败：key={}, error={}", key, e.getMessage());
        }
    }

    @Override
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("自增操作失败：key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public Long decrement(String key) {
        try {
            return redisTemplate.opsForValue().decrement(key);
        } catch (Exception e) {
            log.error("自减操作失败：key={}, error={}", key, e.getMessage());
            return null;
        }
    }
}
