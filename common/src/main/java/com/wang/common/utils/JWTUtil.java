package com.wang.common.utils;


import com.wang.common.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
public class JWTUtil {

    /**
     * JWT密钥
     */
    private static final String JWT_SECRET = "wangwangwangwangwangwangwangwangwangwangwangwang";

    /**
     * JWT 有效时间（毫秒）
     */
    private static final long JWT_EXPIRED = 1000L * 60 * 60 * 24 * 7;

    /**
     * 前缀 - 用于识别项目
     */
    private static final String JWT_PREFIX = "NovelManagementSystem";

    /**
     * subject
     */
    private static final String JWT_SUBJECT = "nms";

    /**
     * 私有构造方法,工具类不能实例化
     */
    private JWTUtil() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }


    /**
     * 生成token
     *
     * @param loginUser 登录用户信息
     * @return token字符串
     */
    public static String geneJsonWebToken(LoginUser loginUser) {
        try {
            String compact = Jwts.builder()
                    .setSubject(JWT_SUBJECT)
                    .claim("id", loginUser.getId())
                    .claim("name", loginUser.getName())
                    .claim("avatar", loginUser.getAvatar())
                    .claim("account", loginUser.getAccount())
                    .claim("role", loginUser.getRole() != null ? loginUser.getRole().getCode() : null)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRED))
                    .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                    .compact();
            return JWT_PREFIX + compact;
        } catch (Exception e) {
            log.error("Token生成失败", e);
            return null;
        }
    }

    /**
     * 校验token
     *
     * @param token 待校验的token
     * @return Claims 载荷信息，校验失败返回null
     */
    public static Claims checkJWT(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token.replace(JWT_PREFIX, ""))
                    .getPayload();
        } catch (Exception e) {
            log.warn("JWT验证失败: {}", e.getMessage());
            return null;
        }
    }
}