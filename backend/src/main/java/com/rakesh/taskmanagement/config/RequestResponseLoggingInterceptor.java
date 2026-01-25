package com.rakesh.taskmanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Request/Response Logging Interceptor
 * 
 * Provides comprehensive logging for all HTTP requests and responses:
 * - Request details (method, URI, user, session)
 * - Response details (status, execution time)
 * - Performance monitoring
 * - User activity tracking
 * - Security event logging
 */
@Slf4j
@Component
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "startTime";
    private static final String REQUEST_ID_ATTRIBUTE = "requestId";
    private static final String USER_ATTRIBUTE = "currentUser";

    /**
     * Pre-handle: Log incoming request details
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Instant startTime = Instant.now();
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        
        // Store timing and tracking info
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);
        request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        
        // Get authenticated user info
        String username = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            username = auth.getName();
            request.setAttribute(USER_ATTRIBUTE, username);
        }
        
        // Log incoming request
        log.info("REQUEST [{}] {} {} - User: {} - IP: {} - User-Agent: {}", 
                 requestId,
                 request.getMethod(), 
                 request.getRequestURI(),
                 username,
                 getClientIpAddress(request),
                 request.getHeader("User-Agent"));
                 
        // Log query parameters if present
        if (request.getQueryString() != null) {
            log.debug("REQUEST [{}] Query Parameters: {}", requestId, request.getQueryString());
        }
        
        return true;
    }

    /**
     * Post-handle: Log after controller execution but before view rendering
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        // Additional processing if needed
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
        log.debug("PROCESSING [{}] Controller execution completed", requestId);
    }

    /**
     * After-completion: Log final response details and performance metrics
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        Instant startTime = (Instant) request.getAttribute(START_TIME_ATTRIBUTE);
        String requestId = (String) request.getAttribute(REQUEST_ID_ATTRIBUTE);
        String username = (String) request.getAttribute(USER_ATTRIBUTE);
        
        if (startTime != null && requestId != null) {
            Duration duration = Duration.between(startTime, Instant.now());
            long executionTimeMs = duration.toMillis();
            
            // Choose log level based on response status
            if (response.getStatus() >= 500) {
                log.error("RESPONSE [{}] {} {} - Status: {} - Time: {}ms - User: {}", 
                         requestId, request.getMethod(), request.getRequestURI(), 
                         response.getStatus(), executionTimeMs, username != null ? username : "anonymous");
            } else if (response.getStatus() >= 400) {
                log.warn("RESPONSE [{}] {} {} - Status: {} - Time: {}ms - User: {}", 
                        requestId, request.getMethod(), request.getRequestURI(), 
                        response.getStatus(), executionTimeMs, username != null ? username : "anonymous");
            } else {
                log.info("RESPONSE [{}] {} {} - Status: {} - Time: {}ms - User: {}", 
                        requestId, request.getMethod(), request.getRequestURI(), 
                        response.getStatus(), executionTimeMs, username != null ? username : "anonymous");
            }
            
            // Log performance warnings for slow requests
            if (executionTimeMs > 5000) { // 5 seconds
                log.warn("SLOW REQUEST [{}] {} {} took {}ms - Consider optimization", 
                        requestId, request.getMethod(), request.getRequestURI(), executionTimeMs);
            } else if (executionTimeMs > 2000) { // 2 seconds
                log.info("MODERATE REQUEST [{}] {} {} took {}ms", 
                        requestId, request.getMethod(), request.getRequestURI(), executionTimeMs);
            }
            
            // Log authentication events
            if (request.getRequestURI().contains("/api/auth/")) {
                logAuthenticationEvent(request, response, username, requestId);
            }
        }
        
        // Log any exceptions that occurred
        if (ex != null) {
            log.error("EXCEPTION [{}] {} {} - Error: {} - User: {}", 
                     requestId, request.getMethod(), request.getRequestURI(), 
                     ex.getMessage(), username != null ? username : "anonymous", ex);
        }
    }

    /**
     * Log specific authentication events for security monitoring
     */
    private void logAuthenticationEvent(HttpServletRequest request, HttpServletResponse response, 
                                      String username, String requestId) {
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();
        String clientIp = getClientIpAddress(request);
        
        if (endpoint.contains("/login")) {
            if (status == 200) {
                log.info("AUTH SUCCESS [{}] User '{}' logged in successfully from IP: {}", 
                        requestId, username != null ? username : "unknown", clientIp);
            } else {
                log.warn("AUTH FAILURE [{}] Failed login attempt from IP: {} - Status: {}", 
                        requestId, clientIp, status);
            }
        } else if (endpoint.contains("/signup")) {
            if (status == 200) {
                log.info("USER REGISTRATION [{}] New user registration successful from IP: {}", 
                        requestId, clientIp);
            } else {
                log.warn("REGISTRATION FAILURE [{}] Failed registration from IP: {} - Status: {}", 
                        requestId, clientIp, status);
            }
        }
    }

    /**
     * Get client IP address, handling proxies and load balancers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !xForwardedFor.equalsIgnoreCase("unknown")) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !xRealIp.equalsIgnoreCase("unknown")) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}