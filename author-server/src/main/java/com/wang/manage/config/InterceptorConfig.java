package com.wang.manage.config;

import com.wang.common.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
//这里采用xdclass_shop中的拦截器编写方式，这样不会破坏SpringBoot的默认配置
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        log.info("开始添加拦截器");
        //registry.addInterceptor(new LoginInterceptor())
               // .addPathPatterns("/author/*/**")
                //登录、登出、注册接口不拦截
               // .excludePathPatterns("/author/login", "/author/logout");
    }

}
