package com.wang.commonserver.service.impl;

import com.wang.common.result.Result;
import com.wang.commonserver.service.EmailService;
import com.wang.common.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(name = "spring.mail.host")
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    /**
     * 验证码过期时间（分钟）
     */
    private static final long CODE_EXPIRE_MINUTES = 10;
    
    /**
     * Redis中验证码的key前缀
     */
    private static final String CODE_KEY_PREFIX = "email:code:";
    
    public EmailServiceImpl(JavaMailSender mailSender, StringRedisTemplate redisTemplate) {
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 发送验证码到邮箱
     * @param email 目标邮箱地址
     * @return 发送结果
     */
    @Override
    public Result sendCode(String email) {
        log.info("发送验证码请求：email={}", email);
        
        // 1. 校验邮箱格式
        if (!CommonUtil.isValidEmail(email)) {
            log.warn("邮箱格式不正确：{}", email);
            return Result.error("邮箱格式不正确");
        }
        
        // 2. 检查是否频繁发送（60秒内只能发送一次）
        String key = CODE_KEY_PREFIX + email;
        String existingCode = redisTemplate.opsForValue().get(key);
        if (existingCode != null) {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl != null && ttl > (CODE_EXPIRE_MINUTES * 60 - 60)) {
                log.warn("验证码发送过于频繁：email={}", email);
                return Result.error("验证码发送过于频繁，请稍后再试");
            }
        }
        
        // 3. 生成6位验证码
        String code = CommonUtil.generateCode(6);
        
        // 4. 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【nms】邮箱验证码");
            message.setText("您的验证码是：" + code + "\n\n验证码有效期10分钟，请尽快使用。\n\n如非本人操作，请忽略此邮件。");
            mailSender.send(message);
            log.info("验证码发送成功：email={}", email);
        } catch (Exception e) {
            log.error("验证码发送失败：email={}, error={}", email, e.getMessage(), e);
            return Result.error("验证码发送失败，请稍后重试");
        }
        
        // 5. 将验证码存入Redis，设置5分钟过期
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        return Result.success("验证码发送成功");
    }

    /**
     * 验证邮箱验证码
     * @param email 邮箱地址
     * @param code 用户输入的验证码
     * @return 验证结果
     */
    @Override
    public Result verifyCode(String email, String code) {
        log.info("验证验证码请求：email={}", email);
        
        // 1. 校验邮箱格式
        if (!CommonUtil.isValidEmail(email)) {
            log.warn("邮箱格式不正确：{}", email);
            return Result.error("邮箱格式不正确");
        }
        
        // 2. 校验验证码是否为空
        if (code == null || code.trim().isEmpty()) {
            log.warn("验证码为空");
            return Result.error("验证码不能为空");
        }
        
        // 3. 从Redis获取验证码
        String key = CODE_KEY_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            log.warn("验证码已过期或不存在：email={}", email);
            return Result.error("验证码已过期，请重新获取");
        }
        
        // 4. 验证码比对
        if (!storedCode.equals(code.trim())) {
            log.warn("验证码错误：email={}, inputCode={}", email, code);
            return Result.error("验证码错误");
        }
        
        // 5. 验证成功，删除验证码（一次性使用）
        redisTemplate.delete(key);
        log.info("验证码验证成功：email={}", email);
        
        return Result.success("验证成功");
    }
}