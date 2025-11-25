package com.rakesh.taskmanagement.config;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl {

    public Optional<String> getCurrentAuditor() {

        // Replace with actual user from Spring Security
        // For now, return "system" - will automatically use real username
        // when Spring Security is implemented

        // FUTURE: When you implement Spring Security, replace with:
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // if (auth != null && auth.isAuthenticated()) {
        //     return Optional.of(auth.getName());
        // }

        return Optional.of("system");
    }
}
