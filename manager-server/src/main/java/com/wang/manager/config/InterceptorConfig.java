package com.wang.manager.config;

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
        log.info("开始添加管理员拦截器");
        // 添加角色拦截器，只允许 MANAGER 角色访问
        registry.addInterceptor(new RoleInterceptor("manager-server", UserRole.MANAGER))
                .addPathPatterns("/manager/*/**")
                // 登录、登出、获取名称和头像接口不拦截
                .excludePathPatterns("/manager/login", "/manager/logout", "/manager/getNameAndAvatar/*");
    }
}