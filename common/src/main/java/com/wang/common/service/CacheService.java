package com.wang.common.service;

import com.wang.common.model.ZSetEntry;

import java.util.List;

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

    /**
     * 添加或更新 Sorted Set 成员
     * @param key 缓存键
     * @param score 分数
     * @param member 成员
     */
    void zAdd(String key, double score, String member);

    /**
     * Sorted Set 成员分数增量
     * @param key 缓存键
     * @param delta 增量（正数增加，负数减少）
     * @param member 成员
     * @return 增量后的分数
     */
    Double zIncrBy(String key, double delta, String member);

    /**
     * 获取 Sorted Set 指定范围的成员和分数（降序，从高到低）
     * @param key 缓存键
     * @param start 起始位置（0开始）
     * @param end 结束位置（-1表示到末尾）
     * @return 成员和分数列表
     */
    List<ZSetEntry> zRevRangeWithScores(String key, long start, long end);

    /**
     * 移除 Sorted Set 成员
     * @param key 缓存键
     * @param member 成员
     */
    void zRem(String key, String member);

    /**
     * 获取 Sorted Set 成员分数
     * @param key 缓存键
     * @param member 成员
     * @return 分数，不存在返回 null
     */
    Double zScore(String key, String member);

    /**
     * 获取 Sorted Set 成员排名（降序，0开始）
     * @param key 缓存键
     * @param member 成员
     * @return 排名，不存在返回 null
     */
    Long zRevRank(String key, String member);
}
