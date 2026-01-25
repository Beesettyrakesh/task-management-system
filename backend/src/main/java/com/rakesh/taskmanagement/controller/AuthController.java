package com.rakesh.taskmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rakesh.taskmanagement.dto.ErrorResponseDto;
import com.rakesh.taskmanagement.dto.LoginRequestDto;
import com.rakesh.taskmanagement.dto.LoginResponseDto;
import com.rakesh.taskmanagement.dto.SignupRequestDto;
import com.rakesh.taskmanagement.dto.SignupResponseDto;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final UserService userService;

    @Operation(
            summary = "Register new user",
            description = "Create a new user account with username, email, and password"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                            content = @Content(schema = @Schema(implementation = SignupResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or user already exists",
                            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        log.info("POST /api/auth/signup - Registration request received for username: {}", 
                 signupRequestDto.getUsername());
        
        try {
            SignupResponseDto response = userService.signup(signupRequestDto);
            log.info("POST /api/auth/signup - Registration successful for username: {} - Response: 200 OK", 
                     signupRequestDto.getUsername());
            return ResponseEntity
                    .ok(response);
        } catch (RuntimeException e) {
            log.warn("POST /api/auth/signup - Registration failed for username: {} - Error: {} - Response: 400 Bad Request", 
                     signupRequestDto.getUsername(), e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponseDto(e.getMessage()));
        }
    }

    @Operation(
            summary = "User login",
            description = "Authenticate user and return JWT token for API access"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful - JWT token returned",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        log.info("POST /api/auth/login - Login request received for username: {}", 
                 loginRequestDto.getUsername());
        
        try {
            LoginResponseDto response = userService.login(loginRequestDto);
            log.info("POST /api/auth/login - Login successful for username: {} - Response: 200 OK", 
                     loginRequestDto.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("POST /api/auth/login - Login failed for username: {} - Error: {} - Response: 401 Unauthorized", 
                     loginRequestDto.getUsername(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponseDto> getCurrentUser(Authentication authentication) {
        log.info("GET /api/auth/me - Current user info request");
        
        try {
            User user = userService.getCurrentUser();

            if (user != null) {
                log.info("GET /api/auth/me - User info retrieved for: {} - Response: 200 OK", user.getUsername());
                LoginResponseDto userInfo = new LoginResponseDto(
                        null,
                        user.getUsername(),
                        user.getEmail());
                return ResponseEntity.ok(userInfo);
            } else {
                log.warn("GET /api/auth/me - User not found - Response: 404 Not Found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.error("GET /api/auth/me - Authentication error: {} - Response: 401 Unauthorized", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
