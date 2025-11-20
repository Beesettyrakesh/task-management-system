package com.rakesh.taskmanagement.controller;

import com.rakesh.taskmanagement.dto.RegisterRequest;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

     private final UserService userService;
     public AuthController(UserService userService) {
         this.userService = userService;
     }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request);
            return ResponseEntity.ok("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
