package com.rakesh.taskmanagement.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.rakesh.taskmanagement.dto.LoginRequestDto;
import com.rakesh.taskmanagement.dto.LoginResponseDto;
import com.rakesh.taskmanagement.dto.SignupRequestDto;
import com.rakesh.taskmanagement.dto.SignupResponseDto;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.exception.ResourceNotFoundException;
import com.rakesh.taskmanagement.repository.UserRepository;
import com.rakesh.taskmanagement.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        log.info("New user registration attempt: username={}, email={}", 
                 signupRequestDto.getUsername(), signupRequestDto.getEmail());
        
        if(userRepository.existsByUsername(signupRequestDto.getUsername())) {
            log.warn("Registration failed: Username '{}' already exists", signupRequestDto.getUsername());
            throw new IllegalArgumentException("Username '" + signupRequestDto.getUsername() + "' is already taken");
        }
        
        if(userRepository.findByEmail(signupRequestDto.getEmail()).isPresent()) {
            log.warn("Registration failed: Email '{}' already exists", signupRequestDto.getEmail());
            throw new IllegalArgumentException("Email '" + signupRequestDto.getEmail() + "' is already registered");
        }

        User user = new User();
        user.setUsername(signupRequestDto.getUsername());
        user.setEmail(signupRequestDto.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(signupRequestDto.getPassword()));
        User savedUser = userRepository.save(user);
        
        log.info("User registration successful: username={}, email={}, ID={}", 
                 savedUser.getUsername(), savedUser.getEmail(), savedUser.getId());

        return new SignupResponseDto(user.getUsername(), user.getEmail());
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        String usernameOrEmail = loginRequestDto.getUsername();
        log.info("Login attempt for: {}", usernameOrEmail);

        User user = null;
        String actualUsername = null;

        if(usernameOrEmail.contains("@")) {
            log.debug("Input appears to be email: {}", usernameOrEmail);
            user = userRepository.findByEmail(usernameOrEmail).orElse(null);

            if(user == null) {
                log.warn("Login failed: Email '{}' not found", usernameOrEmail);
                throw new BadCredentialsException("Email '" + usernameOrEmail + "' not found");
            }

            actualUsername = user.getUsername();
            log.debug("Found user by email. Username: {}", actualUsername);
        } else {
            log.debug("Input appears to be a username: {}", usernameOrEmail);
            user = userRepository.findByUsername(usernameOrEmail).orElse(null);
            
            if (user == null) {
                log.warn("Login failed: Username '{}' not found", usernameOrEmail);
                throw new BadCredentialsException("Username '" + usernameOrEmail + "' not found");
            }
            
            actualUsername = usernameOrEmail;
        }
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(actualUsername, loginRequestDto.getPassword())
            );

            User authenticatedUser = (User) authentication.getPrincipal();
            String token = jwtUtil.generateToken(authenticatedUser.getUsername());
            
            log.info("Login successful for user: {}", loginRequestDto.getUsername());
            return new LoginResponseDto(token, authenticatedUser.getUsername(),  authenticatedUser.getEmail());
            
        } catch (Exception e) {
            log.error("Login failed for username: {} - Reason: {}", 
                      loginRequestDto.getUsername(), e.getMessage());
            throw e;
        }
    }
}
