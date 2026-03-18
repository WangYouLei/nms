package com.wang.commonserver.service.impl;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import com.wang.commonserver.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码辅助类
 * 基于 Hutool Captcha 实现
 */
@Slf4j
@Component
public class CaptchaServiceImpl implements CaptchaService {

    /**
     * 验证码图片宽度
     */
    private static final int WIDTH = 130;

    /**
     * 验证码图片高度
     */
    private static final int HEIGHT = 48;

    /**
     * 验证码位数
     */
    private static final int CODE_COUNT = 4;

    /**
     * 干扰线数量
     */
    private static final int LINE_COUNT = 20;

    /**
     * 验证码过期时间（分钟）
     */
    private static final long EXPIRE_MINUTES = 10;

    /**
     * Redis 中验证码的 key 前缀
     */
    private static final String CODE_KEY_PREFIX = "captcha:";

    private final StringRedisTemplate redisTemplate;

    public CaptchaServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成图形验证码
     * @return Map 包含 token 和 base64 图片
     */
    @Override
    public Map<String, Object> generateCaptcha() {
        // 1. 创建 LineCaptcha 对象（线条干扰的验证码）
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(WIDTH, HEIGHT, CODE_COUNT, LINE_COUNT);

        // 2. 获取验证码文本
        String code = captcha.getCode();

        log.info("图形验证码：code={}",code);

        // 3. 生成唯一 token（用于前端识别本次验证）
        String token = IdUtil.fastSimpleUUID();

        // 4. 存储到 Redis，设置 10 分钟过期
        String key = CODE_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(
                key,
                code.toLowerCase(),
                EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        log.debug("生成验证码：token={}, code={}", token, code);

        // 5. 返回 token 和 base64 格式的图片
        return Map.of(
                "token", token,
                "image", captcha.getImageBase64Data()
        );
    }

    /**
     * 校验验证码
     * @param token 验证码 token
     * @param code 用户输入的验证码
     * @return 是否验证成功
     */
    @Override
    public boolean verify(String token, String code) {
        if (token == null || token.trim().isEmpty() ||
                code == null || code.trim().isEmpty()) {
            return false;
        }

        String key = CODE_KEY_PREFIX + token.trim();
        String storedCode = redisTemplate.opsForValue().get(key);

        // 验证码不存在或已过期
        if (storedCode == null) {
            log.warn("验证码已过期或不存在：token={}", token);
            return false;
        }

        // 忽略大小写比对
        boolean result = storedCode.equals(code.trim().toLowerCase());

        if (result) {
            // 验证成功后删除验证码（一次性使用）
            redisTemplate.delete(key);
            log.debug("验证码验证成功：token={}", token);
        } else {
            log.warn("验证码错误：token={}, input={}", token, code);
        }

        return result;
    }

    /**
     * 删除验证码（用于手动失效）
     * @param token 验证码 token
     */
    @Override
    public void removeCaptcha(String token) {
        if (token != null && !token.trim().isEmpty()) {
            String key = CODE_KEY_PREFIX + token.trim();
            redisTemplate.delete(key);
        }
    }
}

