package com.wang.common.interceptor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.wang.common.utils.RoleContextUtil;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.utils.CommonUtil;
import com.wang.common.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色拦截器
 * 通过构造函数指定允许访问的角色列表
 * 使用 RoleContextUtil 存储用户信息，服务层可通过 RoleContextUtil.getCurrentUser() 获取
 */
@Slf4j
public class RoleInterceptor implements HandlerInterceptor {

    private final Set<UserRole> allowedRoles;
    private final String moduleName;

    /**
     * 构造函数
     * @param moduleName 模块名称（用于日志）
     * @param roles 允许访问的角色列表
     */
    public RoleInterceptor(String moduleName, UserRole... roles) {
        this.moduleName = moduleName;
        this.allowedRoles = new HashSet<>(Arrays.asList(roles));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            //前端传来的token可能在请求头，也可能在Get请求类型的请求参数中
            String accessToken = request.getHeader("token");
            if (accessToken == null) {
                accessToken = request.getParameter("token");
            }

            if (StringUtils.isNotBlank(accessToken)) {
                Claims claims = JWTUtil.checkJWT(accessToken);
                if (claims == null) {
                    //告诉登录过期，重新登录
                    CommonUtil.sendJsonMessage(response, Result.buildResult(BizCodeEnum.USER_NOT_LOGIN));
                    return false;
                }

                Integer id = Integer.valueOf(claims.get("id").toString());
                String avatar = (String) claims.get("avatar");
                String name = (String) claims.get("name");
                String account = (String) claims.get("account");
                String roleCode = (String) claims.get("role");
                UserRole role = UserRole.fromCode(roleCode);

                // 角色校验
                if (role == null || !allowedRoles.contains(role)) {
                    log.warn("[{}] 非法角色访问：account={}, role={}, 允许的角色={}", 
                            moduleName, account, roleCode, allowedRoles);
                    CommonUtil.sendJsonMessage(response, Result.buildResult(BizCodeEnum.PERMISSION_DENIED));
                    return false;
                }

                LoginUser loginUser = LoginUser.builder()
                        .id(id)
                        .name(name)
                        .account(account)
                        .avatar(avatar)
                        .role(role)
                        .build();

                // 用户信息存储到 RoleContextUtil，服务层可通过 RoleContextUtil.getCurrentUser() 获取
                RoleContextUtil.setCurrentUser(loginUser);
                return true;
            }

        } catch (Exception e) {
            log.error("[{}] 拦截器异常：{}", moduleName, e.getMessage());
        }

        CommonUtil.sendJsonMessage(response, Result.buildResult(BizCodeEnum.USER_NOT_LOGIN));
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 线程变量销毁，防止内存泄漏
        RoleContextUtil.clear();
    }
}