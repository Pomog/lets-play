package com.example.letsplay;

import com.example.letsplay.controller.AuthController;
import com.example.letsplay.model.User;
import com.example.letsplay.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    UserService userService;
    
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
    
}
