package com.wang.common.untils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.util.Random;
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
     * 工具类禁止实例化
     */
    private CommonUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
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
     * 生成指定长度的随机数字验证码
     * @param length 验证码长度
     * @return 验证码字符串
     */
    public static String generateCode(int length) {
        if (length <= 0) {
            length = 6;
        }
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送json数据给前端
     * @param response
     * @param object
     */
    public static void sendJsonMessage(HttpServletResponse response, Object object){
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType("application/json;charset=utf-8");//设置响应头类型
        try(PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(object));
            response.flushBuffer();
        }catch (Exception e){
            /*log.warn("响应json失败");*/
            System.out.println("响应json失败");
        }
    }
    /**
     * uuid随机生成字符串
     * @return
     */
    public static String getRandomString(Integer  length){
        return UUID.randomUUID().toString().replaceAll("-","").substring(0,length);
    }


}
