package com.wang.common.untils;


import com.wang.common.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;


@Slf4j
public class JWTUtil {
    /**
     * JWT密钥      企业级开发中，秘钥不能泄露，且长度不能低于128位
     */
    private static final String JWT_SECRET = "wang"; //这里就先简单设置一下

    /**
     * JWT 有效时间   通常是设置7天
     */
    private static final long JWT_EXPIRED = 1000 * 60 * 60 * 24 * 7;  // 7天

    /**
     * 前缀   这个不是token里的，只是为了方便识别项目加到生成好的token前，不是必须的（解密时要去掉）
     */
    private static final String JWT_PREFIX = "NovelManagementSystem";

    /**
     * subject
     */
    private static final String JWT_SUBJECT = "nms";

    /**
     * 生成token
     * @param loginUser
     * @return
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
                    //设置过期时间
                    .setIssuedAt(new Date())//设置当前时间
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRED))//设置过期时间
                    .signWith(SignatureAlgorithm.HS256, JWT_SECRET)//设置加密算法和密钥
                    .compact();
            return JWT_PREFIX + compact;
        }catch (Exception e){
            log.info("token无法生成");
            return null;
        }
    }

    /**
     * 校验token
     * @param token
     * @return
     */
    public static Claims checkJWT(String token) {
        try {

            final Claims claims = Jwts.parser().setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token.replace(JWT_PREFIX, "")).getBody();

            return claims;

        } catch (Exception e) {
            return null;
        }
    }
}