package com.wang.gateway.filter;

import com.wang.gateway.config.AuthRouteProperties;
import com.wang.gateway.config.AuthRouteProperties.AuthRule;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);
    private static final String JWT_SECRET = "aB3$dE6&gH9*jK2@mN5#pQ8$rT1^wZ4!yC7";
    private static final String JWT_PREFIX = "NovelManagementSystem";
    private static final String GATEWAY_AUTH_SECRET = "NmsGateway2024!@#";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    private AuthRouteProperties authRouteProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        AuthRule matchedRule = findMatchingRule(path);
        if (matchedRule == null) {
            return chain.filter(exchange);
        }

        if (isExcluded(path, matchedRule)) {
            return chain.filter(exchange);
        }

        // 从请求头或查询参数获取token
        String token = exchange.getRequest().getHeaders().getFirst("token");
        if (token == null || token.isEmpty()) {
            List<String> tokenParams = exchange.getRequest().getQueryParams().get("token");
            if (tokenParams != null && !tokenParams.isEmpty()) {
                token = tokenParams.get(0);
            }
        }

        if (token == null || token.isEmpty()) {
            log.warn("鉴权失败：未提供token, path={}", path);
            return writeErrorResponse(exchange, 20004, "用户未登录");
        }

        Claims claims = checkJWT(token);
        if (claims == null) {
            log.warn("鉴权失败：token无效, path={}", path);
            return writeErrorResponse(exchange, 20004, "用户未登录");
        }

        String roleCode = (String) claims.get("role");
        List<String> allowedRoles = matchedRule.getAllowedRoles();
        if (roleCode == null || !allowedRoles.contains(roleCode)) {
            log.warn("鉴权失败：角色不匹配, path={}, role={}, allowedRoles={}", path, roleCode, allowedRoles);
            return writeErrorResponse(exchange, 10006, "权限不足");
        }

        // 将用户信息写入请求头，传递给下游服务
        Object idClaim = claims.get("id");
        if (idClaim == null) {
            log.warn("鉴权失败：JWT中缺少id字段, path={}", path);
            return writeErrorResponse(exchange, 20004, "用户未登录");
        }

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(idClaim))
                .header("X-User-Name", (String) claims.get("name"))
                .header("X-User-Avatar", (String) claims.get("avatar"))
                .header("X-User-Account", (String) claims.get("account"))
                .header("X-User-Role", roleCode)
                .header("X-Gateway-Auth", GATEWAY_AUTH_SECRET)
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    private AuthRule findMatchingRule(String path) {
        for (AuthRule rule : authRouteProperties.getRules()) {
            for (String pattern : rule.getPathPatterns()) {
                if (pathMatcher.match(pattern, path)) {
                    return rule;
                }
            }
        }
        return null;
    }

    private boolean isExcluded(String path, AuthRule rule) {
        for (String excludePath : rule.getExcludePaths()) {
            if (pathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    private Claims checkJWT(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token.replace(JWT_PREFIX, ""))
                    .getPayload();
        } catch (Exception e) {
            log.warn("JWT验证失败: {}", e.getMessage());
            return null;
        }
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, int code, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":" + code + ",\"msg\":\"" + msg + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
