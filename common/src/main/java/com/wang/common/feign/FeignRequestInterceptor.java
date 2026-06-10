package com.wang.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 传递网关设置的用户信息头
            String userId = request.getHeader("X-User-Id");
            if (userId != null) {
                requestTemplate.header("X-User-Id", userId);
                requestTemplate.header("X-User-Name", request.getHeader("X-User-Name"));
                requestTemplate.header("X-User-Avatar", request.getHeader("X-User-Avatar"));
                requestTemplate.header("X-User-Account", request.getHeader("X-User-Account"));
                requestTemplate.header("X-User-Role", request.getHeader("X-User-Role"));
                requestTemplate.header("X-Gateway-Auth", request.getHeader("X-Gateway-Auth"));
            }
        } else {
            log.debug("Feign 调用时无法获取当前请求上下文");
        }
    }
}
