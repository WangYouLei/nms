package com.wang.visitor.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器
 * 在服务间调用时自动传递当前请求的 token
 */
@Slf4j
@Configuration
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 获取当前请求的属性
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 获取 token
            String token = request.getHeader("token");
            if (token != null) {
                log.debug("Feign 调用传递 token");
                requestTemplate.header("token", token);
            } else {
                log.debug("Feign 调用时未找到 token");
            }
        } else {
            log.debug("Feign 调用时无法获取当前请求上下文");
        }
    }
}
