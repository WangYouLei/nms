package com.wang.common.service;

/**
 * Token 管理服务接口
 * 用于管理用户登录 token 的存储和删除
 */
public interface TokenService {

    /**
     * 存储 token 到 Redis
     * @param key token 的唯一标识（如 "visitor:123"）
     * @param token JWT token
     * @param expireSeconds 过期时间（秒）
     */
    void saveToken(String key, String token, long expireSeconds);

    /**
     * 获取 token
     * @param key token 的唯一标识
     * @return token，不存在返回 null
     */
    String getToken(String key);

    /**
     * 删除 token
     * @param key token 的唯一标识
     */
    void deleteToken(String key);

    /**
     * 删除用户的所有 token（当用户切换端或退出时）
     * @param role 用户角色（visitor/author/manager）
     * @param userId 用户ID
     */
    void deleteUserTokens(String role, Long userId);

    /**
     * 验证 token 是否有效
     * @param key token 的唯一标识
     * @param token 要验证的 token
     * @return 是否有效
     */
    boolean validateToken(String key, String token);

    /**
     * 生成 token 的 Redis key
     * @param role 用户角色
     * @param userId 用户ID
     * @return Redis key
     */
    String generateTokenKey(String role, Long userId);
}
