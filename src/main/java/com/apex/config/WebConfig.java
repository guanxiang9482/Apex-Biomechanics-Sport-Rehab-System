package com.apex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the RoleGuard interceptor so it runs on every
 * /api/** request, excluding public /api/auth/** paths.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RoleGuard roleGuard;

    public WebConfig(RoleGuard roleGuard) {
        this.roleGuard = roleGuard;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleGuard)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }
}
