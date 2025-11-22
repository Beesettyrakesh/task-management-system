package com.rakesh.taskmanagement.service;

import com.rakesh.taskmanagement.dto.LoginRequestDto;
import com.rakesh.taskmanagement.dto.LoginResponseDto;
import com.rakesh.taskmanagement.dto.SignupRequestDto;
import com.rakesh.taskmanagement.dto.SignupResponseDto;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.repository.UserRepository;
import com.rakesh.taskmanagement.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        if(userRepository.existsByUsername(signupRequestDto.getUsername())) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = new User();
        user.setUsername(signupRequestDto.getUsername());
        user.setEmail(signupRequestDto.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(signupRequestDto.getPassword()));
        userRepository.save(user);

        return new SignupResponseDto(user.getUsername(), user.getEmail());
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user.getUsername());
        return new LoginResponseDto(token, user.getUsername(),  user.getEmail());
    }
}
