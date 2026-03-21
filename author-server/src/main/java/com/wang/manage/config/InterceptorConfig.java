package com.wang.manage.config;

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
        log.info("开始添加作者拦截器");
        // 添加角色拦截器，只允许 AUTHOR 角色访问
        registry.addInterceptor(new RoleInterceptor("author-server", UserRole.AUTHOR))
                .addPathPatterns("/author/*/**")
                // 登录、登出、注册接口不拦截
                .excludePathPatterns("/author/login", "/author/logout", "/author/register");
    }
}
