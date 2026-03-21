package com.wang.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
public class CommonUtil {

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /**
     * 安全随机数生成器
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 工具类禁止实例化
     */
    private CommonUtil() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }

    /**
     * 校验邮箱格式是否正确
     * @param email 邮箱地址
     * @return true-格式正确，false-格式错误
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * 生成指定长度的随机数字验证码（使用安全随机数）
     * @param length 验证码长度
     * @return 验证码字符串
     */
    public static String generateCode(int length) {
        if (length <= 0) {
            length = 6;
        }
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(SECURE_RANDOM.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送json数据给前端
     * @param response HTTP响应
     * @param object 响应对象
     */
    public static void sendJsonMessage(HttpServletResponse response, Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType("application/json;charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(object));
            response.flushBuffer();
        } catch (Exception e) {
            log.warn("响应json失败: {}", e.getMessage());
        }
    }

    /**
     * uuid随机生成字符串
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String getRandomString(Integer length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }
}
