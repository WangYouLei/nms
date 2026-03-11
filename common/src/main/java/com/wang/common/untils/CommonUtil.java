package com.wang.common.untils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class CommonUtil {

    /**
     * 工具类禁止实例化
     */
    private CommonUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
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
