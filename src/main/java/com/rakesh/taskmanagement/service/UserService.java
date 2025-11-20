package com.rakesh.taskmanagement.service;

import com.rakesh.taskmanagement.dto.RegisterRequest;
import com.rakesh.taskmanagement.entity.User;
import com.rakesh.taskmanagement.exception.ResourceNotFoundException;
import com.rakesh.taskmanagement.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository,  BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User registerUser(RegisterRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceNotFoundException("User already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }
}
