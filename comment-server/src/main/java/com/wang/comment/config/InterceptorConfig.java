package com.wang.comment.config;

import com.wang.common.enums.UserRole;
import com.wang.common.interceptor.RoleInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        log.info("开始添加访客拦截器");
        // 添加角色拦截器，author、manager、visitor都可以访问
        registry.addInterceptor(new RoleInterceptor("comment-server", UserRole.VISITOR,UserRole.MANAGER,UserRole.AUTHOR))
                .addPathPatterns("/comment/*/**");
    }
}
