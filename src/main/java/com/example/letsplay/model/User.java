package com.example.letsplay.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "users")
@Data
public class User {
    @Id
    private String id;
    
    private String name;
    private String email;
    
    @JsonIgnore
    private String password;
    
    private Set<String> roles;
    
    public User() {
    }
    
    public User(String name, String email, String password, Set<String> roles) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }
}