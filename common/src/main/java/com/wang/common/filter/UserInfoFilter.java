package com.wang.common.filter;

import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.utils.RoleContextUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 用户信息过滤器
 * 从网关传递的请求头中读取用户信息，设置到ThreadLocal供服务层使用
 * 通过 X-Gateway-Auth 头验证请求确实来自网关，防止请求头伪造
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserInfoFilter implements Filter {

    private static final String GATEWAY_AUTH_SECRET = "NmsGateway2024!@#";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String gatewayAuth = httpRequest.getHeader("X-Gateway-Auth");
        String userId = httpRequest.getHeader("X-User-Id");

        if (StringUtils.hasText(userId) && GATEWAY_AUTH_SECRET.equals(gatewayAuth)) {
            try {
                String roleHeader = httpRequest.getHeader("X-User-Role");
                UserRole role = StringUtils.hasText(roleHeader) ? UserRole.fromCode(roleHeader) : null;
                LoginUser loginUser = LoginUser.builder()
                        .id(Long.valueOf(userId))
                        .name(httpRequest.getHeader("X-User-Name"))
                        .avatar(httpRequest.getHeader("X-User-Avatar"))
                        .account(httpRequest.getHeader("X-User-Account"))
                        .role(role)
                        .build();
                RoleContextUtil.setCurrentUser(loginUser);
            } catch (NumberFormatException e) {
                log.warn("X-User-Id 格式无效: {}", userId);
            }
        } else if (StringUtils.hasText(userId)) {
            log.warn("X-User-Id 存在但 X-Gateway-Auth 校验失败，疑似请求头伪造");
        }

        try {
            chain.doFilter(request, response);
        } finally {
            RoleContextUtil.clear();
        }
    }
}
