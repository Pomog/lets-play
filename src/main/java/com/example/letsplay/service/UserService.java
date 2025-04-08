package com.example.letsplay.service;

import com.example.letsplay.dto.LoginRequest;
import com.example.letsplay.dto.RegisterRequest;
import com.example.letsplay.model.User;
import com.example.letsplay.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;  //this bean in security config
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    
    public UserService(UserRepository userRepo, BCryptPasswordEncoder encoder,
                       AuthenticationManager authManager, JwtUtil jwtUtil) {
        this.userRepository = userRepo;
        this.encoder = encoder;
        this.authenticationManager = authManager;
        this.jwtUtil = jwtUtil;
    }
    
    public User registerUser(RegisterRequest request) {
        // Check duplicate
        if (userRepository.existsByEmail(request.getEmail())) {
// TODO : throw a custom exception (e.g., EmailAlreadyExistsException) and handle it to return 400
            throw new RuntimeException("Email already in use");
        }

        User user = new User(request.getName(), request.getEmail(),
                encoder.encode(request.getPassword()),
                Set.of("USER"));
        return userRepository.save(user);
    }
    
    public String login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            org.springframework.security.core.userdetails.User userDetails =
                    (org.springframework.security.core.userdetails.User) auth.getPrincipal();
            String username = userDetails.getUsername();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            return jwtUtil.generateTokenFromUsername(username, roles);
        } catch (Exception e) {
            throw new RuntimeException("Invalid credentials");
        }
    }
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }
}