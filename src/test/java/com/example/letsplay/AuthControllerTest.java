package com.example.letsplay;

import com.example.letsplay.config.SecurityConfig;
import com.example.letsplay.controller.AuthController;
import com.example.letsplay.exception.InvalidCredentialsException;
import com.example.letsplay.model.User;
import com.example.letsplay.service.AuthService;
import com.example.letsplay.service.JwtUtil;
import com.example.letsplay.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean(name = "userService")
    UserService userService;
    
    @MockitoBean
    AuthService authService;
    
    @MockitoBean
    private JwtUtil jwtUtil;
    
    
    @WithMockUser
    @Test
    void testRegisterUser_success() throws Exception {
        String json = "{ \"name\":\"John Doe\", \"email\":\"john@example.com\", \"password\":\"secret123\" }";
        // We expect on success maybe a 201 or 200 and user info minus password.
        User savedUser = new User("John Doe", "john@example.com", "<hashed>", Set.of("USER"));
        savedUser.setId("u123");
        when(userService.registerUser(any())).thenReturn(savedUser);
        
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }
    
    @WithMockUser
    @Test
    void testRegisterUser_duplicateEmail() throws Exception {
        String json = "{ \"name\":\"Jane\", \"email\":\"john@example.com\", \"password\":\"pwd\" }";
        when(userService.registerUser(any()))
                .thenThrow(new RuntimeException("Email already in use"));  // we'll refine exception type later
        
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testLogin_success() throws Exception {
        String json = "{ \"email\":\"john@example.com\", \"password\":\"secret123\" }";
        String token = "fake-jwt-token";
        when(authService.login("john@example.com", "secret123")).thenReturn(token);
        
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(token));
    }
    
    @Test
    void testLogin_invalidCredentials() throws Exception {
        String json = "{ \"email\":\"john@example.com\", \"password\":\"wrong\" }";
        when(authService.login("john@example.com", "wrong"))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));
        
        
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
    
    
}
