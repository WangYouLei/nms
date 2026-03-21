package com.wang.novel.config;

import com.wang.common.enums.UserRole;
import com.wang.common.interceptor.RoleInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始添加小说服务拦截器");

        // 1. /manager/** 路径 - 只允许 MANAGER 访问
        registry.addInterceptor(new RoleInterceptor("novel-server/manager", UserRole.MANAGER))
                .addPathPatterns("/manager/**");

        // 2. /author/** 路径 - 只允许 AUTHOR 访问
        registry.addInterceptor(new RoleInterceptor("novel-server/author", UserRole.AUTHOR))
                .addPathPatterns("/author/**");

        // 3. /visitor/** 路径 - 只允许 VISITOR 访问
        registry.addInterceptor(new RoleInterceptor("novel-server/visitor", UserRole.VISITOR))
                .addPathPatterns("/visitor/**");

        // 4. /common/** 路径 - 允许所有登录者访问
        registry.addInterceptor(new RoleInterceptor("novel-server/common", 
                        UserRole.AUTHOR, UserRole.MANAGER, UserRole.VISITOR))
                .addPathPatterns("/common/**");
    }
}
