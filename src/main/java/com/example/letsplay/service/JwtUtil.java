package com.example.letsplay.service;

import com.example.letsplay.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    
    // TODO : store SECRET_KEY in application properties or a vault
    private static final String SECRET_KEY = "MySuperSecretKeyMySuperSecretKey";
    private static final long EXPIRATION_MS = 3600_000; // 1 hour
    
    private final Key key;
    
    public JwtUtil() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Generates a JWT for the given user, using the user's email as the subject
     * and adding the user's roles as a claim.
     */
    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getEmail())          // Subject = user's email
                .claim("roles", user.getRoles())   // Add roles as a claim
                .issuedAt(new Date(now))           // Token creation time
                .expiration(new Date(now + EXPIRATION_MS)) // Expiration time
                .signWith(key)                     // Use the Key; algorithm is inferred
                .compact();
    }
    
    /**
     * Generates a JWT using a plain username and a list of roles.
     */
    public String generateTokenFromUsername(String username, List<String> roles) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date(now))
                .expiration(new Date(now + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }
    
    /**
     * Validates the token signature and expiration.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException covers signature validation and token format errors
            // IllegalArgumentException can occur for invalid arguments
            return false;
        }
    }
    
    /**
     * Extracts the subject (in this case, the user’s email) from the token.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
    
    /**
     * Extracts the "roles" claim from the token as a list of strings.
     */
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Object roles = claims.get("roles");
        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
