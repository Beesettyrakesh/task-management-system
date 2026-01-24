package com.rakesh.taskmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
        try {
            SignupResponseDto response = userService.signup(signupRequestDto);
            return ResponseEntity
                    .ok(response);
        } catch (RuntimeException e) {
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
        return ResponseEntity.ok(userService.login(loginRequestDto));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponseDto> getCurrentUser(Authentication authentication) {
        try {
            User user = userService.getCurrentUser();

            if (user != null) {
                LoginResponseDto userInfo = new LoginResponseDto(
                        null,
                        user.getUsername(),
                        user.getEmail());
                return ResponseEntity.ok(userInfo);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
