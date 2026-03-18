package com.wang.common.untils;


import com.wang.common.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

@Slf4j
public class JWTUtil {
    /**
     * JWT密钥      HS256 算法要求密钥长度至少为 256 位（32 字节）
     */
    // 至少32个字符

    private static final  String JWT_SECRET = "wangwangwangwangwangwangwangwangwangwangwangwang";

    /**
     * JWT 有效时间   通常是设置7天
     */
    // 7天
    private static final long JWT_EXPIRED = 1000L * 60 * 60 * 24 * 7;

    /**
     * 前缀   这个不是token里的，只是为了方便识别项目加到生成好的token前，不是必须的（解密时要去掉）
     */
    private static final String JWT_PREFIX = "NovelManagementSystem";

    /**
     * subject
     */
    private static final String JWT_SUBJECT = "nms";

    /**
     * 私有构造方法,工具类不能实例化
     */
    private JWTUtil(){
        throw new UnsupportedOperationException("工具类不能被实例化");
    }
    /**
     * 生成token
     *
     * @param loginUser
     * @return string
     */
    public static String geneJsonWebToken(LoginUser loginUser) {
        try {
            String compact = Jwts.builder()
                    //payload  设置负载（也就是要存放的信息,这里注意不要放敏感信息：比如密码等）
                    .setSubject(JWT_SUBJECT)//设置主题
                    .claim("id", loginUser.getId())
                    .claim("name", loginUser.getName())
                    .claim("avatar", loginUser.getAvatar())
                    .claim("account", loginUser.getAccount())
                    //设置当前时间
                    .setIssuedAt(new Date())
                    //设置过期时间
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRED))
                    //设置加密算法和密钥
                    .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes()), SignatureAlgorithm.HS256)
                    .compact();
            return JWT_PREFIX + compact;
        } catch (Exception e) {
            log.info("token无法生成:{}", e);
            return null;
        }
    }


    /**
     * 校验token
     *
     * @param token
     * @return
     */
    public static Claims checkJWT(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes()))
                    .build()
                    .parseSignedClaims(token.replace(JWT_PREFIX, ""))
                    .getPayload();
        } catch (Exception e) {
            log.warn("JWT验证失败: ", e);
            return null;
        }
    }

}
