package com.wang.common.service;

/**
 * 通用缓存服务接口
 * 提供统一的缓存操作方法
 */
public interface CacheService {

    /**
     * 获取缓存
     * @param key 缓存键
     * @return 缓存值，不存在返回 null
     */
    Object get(String key);

    /**
     * 获取缓存并转换为指定类型
     * @param key 缓存键
     * @param clazz 目标类型
     * @return 缓存值，不存在返回 null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     * @param ttlSeconds 过期时间（秒）
     */
    void set(String key, Object value, long ttlSeconds);

    /**
     * 设置缓存（使用默认过期时间）
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * 删除缓存
     * @param key 缓存键
     */
    void delete(String key);

    /**
     * 批量删除缓存（支持通配符）
     * @param pattern 匹配模式，如 "novel:detail:*"
     */
    void deleteByPattern(String pattern);

    /**
     * 检查缓存是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 设置过期时间
     * @param key 缓存键
     * @param ttlSeconds 过期时间（秒）
     */
    void expire(String key, long ttlSeconds);

    /**
     * 自增
     * @param key 缓存键
     * @return 自增后的值
     */
    Long increment(String key);

    /**
     * 自减
     * @param key 缓存键
     * @return 自减后的值
     */
    Long decrement(String key);
}
