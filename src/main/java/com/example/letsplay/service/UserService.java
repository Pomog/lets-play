package com.example.letsplay.service;

import com.example.letsplay.dto.RegisterRequest;
import com.example.letsplay.model.User;
import com.example.letsplay.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;  //this bean in security config
    
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public User registerUser(RegisterRequest request) {
        // Check duplicate
        if (userRepository.existsByEmail(request.getEmail())) {
// TODO : throw a custom exception (e.g., EmailAlreadyExistsException) and handle it to return 400
            throw new RuntimeException("Email already in use");
        }

        User user = new User(request.getName(), request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Set.of("USER"));
        return userRepository.save(user);
    }
    
}