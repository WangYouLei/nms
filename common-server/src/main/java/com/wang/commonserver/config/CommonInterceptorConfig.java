package com.wang.commonserver.config;

import com.wang.common.enums.UserRole;
import com.wang.common.interceptor.RoleInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class CommonInterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        log.info("开始添加访客拦截器");
        // 添加角色拦截器，所有角色都可以访问
        registry.addInterceptor(new RoleInterceptor("common-server", UserRole.VISITOR,UserRole.AUTHOR,UserRole.MANAGER))
                // 拦截文件相关接口    邮箱和图形验证码相关接口不拦截
                .addPathPatterns("/file/**")
                // 获取预签名URL接口不拦截（用于前端显示图片）
                .excludePathPatterns("/file/presigned-url");
    }
}
