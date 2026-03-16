package com.wang.manager.config;

import com.wang.common.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        log.info("开始添加拦截器");
        // 添加登录拦截器
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/manager/*/**")
                // 登录、登出接口不拦截
                .excludePathPatterns("/manager/login", "/manager/logout");
    }
}