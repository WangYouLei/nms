package com.wang.visitor.config;

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
        // 添加角色拦截器，只允许 VISITOR 角色访问
        registry.addInterceptor(new RoleInterceptor("visitor-server", UserRole.VISITOR))
                .addPathPatterns("/visitor/*/**")
                // 登录、登出、注册、获取名称和头像接口不拦截
                .excludePathPatterns("/visitor/login", "/visitor/logout", "/visitor/register", "/visitor/getNameAndAvatar/*");
    }
}
