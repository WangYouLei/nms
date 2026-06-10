package com.wang.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "auth")
public class AuthRouteProperties {

    private List<AuthRule> rules = new ArrayList<>();

    public List<AuthRule> getRules() {
        return rules;
    }

    public void setRules(List<AuthRule> rules) {
        this.rules = rules;
    }

    public static class AuthRule {
        private List<String> pathPatterns = new ArrayList<>();
        private List<String> allowedRoles = new ArrayList<>();
        private List<String> excludePaths = new ArrayList<>();

        public List<String> getPathPatterns() {
            return pathPatterns;
        }

        public void setPathPatterns(List<String> pathPatterns) {
            this.pathPatterns = pathPatterns;
        }

        public List<String> getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(List<String> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }

        public List<String> getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(List<String> excludePaths) {
            this.excludePaths = excludePaths;
        }
    }
}
