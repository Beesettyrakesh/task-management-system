package com.rakesh.taskmanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {
    @GetMapping("/api/debug/headers")
    public String debugHeaders(HttpServletRequest request) {
        return "Scheme: " + request.getScheme() + "\n" +
                "ServerName: " + request.getServerName() + "\n" +
                "ServerPort: " + request.getServerPort() + "\n" +
                "X-Forwarded-Proto: " + request.getHeader("X-Forwarded-Proto") + "\n" +
                "X-Forwarded-Port: " + request.getHeader("X-Forwarded-Port") + "\n" +
                "IsSecure: " + request.isSecure();
    }
}
