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
                .addPathPatterns("/api/**") // Apply to all API endpoints
                .excludePathPatterns(
                    "/api/docs/**",           // Exclude Swagger docs
                    "/api-docs/**",           // Exclude OpenAPI docs  
                    "/swagger-ui/**",         // Exclude Swagger UI
                    "/swagger-resources/**",  // Exclude Swagger resources
                    "/webjars/**"            // Exclude static resources
                );
    }
}