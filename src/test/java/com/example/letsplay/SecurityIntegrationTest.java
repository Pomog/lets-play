package com.example.letsplay;

import com.example.letsplay.model.User;
import com.example.letsplay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This integration test uses the full application context to verify
 * Spring Security behavior (e.g., authentication, roles, etc.) via MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepo;
    @Autowired
    BCryptPasswordEncoder encoder;
    
    /**
     * Sets up the database with a "normal" user (ROLE_USER)
     * and an "admin" user (ROLE_ADMIN, ROLE_USER) before each test.
     */
    @BeforeEach
    void setupUser() {
        // Clear existing users to start fresh
        userRepo.deleteAll();
        
        User normalUser = new User("Alice", "alice@example.com", encoder.encode("pass123"), Set.of("USER"));
        User adminUser = new User("Bob", "bob@example.com", encoder.encode("admin123"), Set.of("ADMIN","USER"));
        userRepo.save(normalUser);
        userRepo.save(adminUser);
    }
    
    /**
     * Helper method that logs in using /api/auth/login
     * and returns the JWT token from the response body.
     */
    String obtainToken(String email, String password) throws Exception {
        String loginJson = "{ \"email\": \""+email+"\", \"password\": \""+password+"\" }";
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }
    
    @Test
    void testProtectedEndpoints() throws Exception {
        // 1) GET /api/users while not authenticated => 401 Unauthorized
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
        
        // 2) GET /api/users as normal user => 403 Forbidden (requires ADMIN)
        String userToken = obtainToken("alice@example.com", "pass123");
        mockMvc.perform(
                        get("/api/users")
                                .header("Authorization", "Bearer " + userToken)
                )
                .andExpect(status().isForbidden());
        
        // 3) GET /api/users as admin => 200 OK
        String adminToken = obtainToken("bob@example.com", "admin123");
        mockMvc.perform(
                        get("/api/users")
                                .header("Authorization", "Bearer " + adminToken)
                )
                .andExpect(status().isOk());
    }
}

