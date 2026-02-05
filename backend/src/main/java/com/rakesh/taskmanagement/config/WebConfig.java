package com.rakesh.taskmanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration for Spring MVC
 * 
 * Configures interceptors and other web-related settings:
 * - Request/Response logging interceptor
 * - Performance monitoring
 * - Security event tracking
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestResponseLoggingInterceptor requestResponseLoggingInterceptor;

    /**
     * Register interceptors for request/response logging and monitoring
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestResponseLoggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/docs/**",
                    "/api-docs/**",
                        "/v3/api-docs/**",
                    "/swagger-ui/**",
                        "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**"
                );
    }
}