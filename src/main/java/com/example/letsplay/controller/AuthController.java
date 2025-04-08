package com.example.letsplay.controller;

import com.example.letsplay.dto.LoginRequest;
import com.example.letsplay.dto.RegisterRequest;
import com.example.letsplay.model.User;
import com.example.letsplay.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegisterRequest request) {
        User created = userService.registerUser(request);
        return ResponseEntity.status(201).body(created);
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.ok(token);
    }

}
