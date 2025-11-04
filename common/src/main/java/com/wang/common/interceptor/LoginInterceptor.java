package com.wang.common.interceptor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.untils.CommonUtil;
import com.wang.common.untils.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 登录拦截器
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    public static  ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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

                LoginUser loginUser = LoginUser.builder()
                        .id(id)
                        .name(name)
                        .account( account)
                        .avatar(avatar)
                        .build();

//                loginUser.setId(id);
//                loginUser.setName(name);
//                loginUser.setHeaderImg(headImg);
//                loginUser.setMail(mail);

                //用户信息传递,使用ThreadLocal   后面要获取时，直接从threadLocal.get()获取就行
                threadLocal.set(loginUser);

                return true;

            }

        } catch (Exception e) {
            log.error("拦截器错误:{}",e);
        }


        CommonUtil.sendJsonMessage(response, Result.buildResult(BizCodeEnum.USER_NOT_LOGIN));
        return false;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
