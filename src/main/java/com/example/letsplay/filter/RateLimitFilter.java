package com.example.letsplay.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW_MS = 60000;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String clientIP = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        lastRequestTime.putIfAbsent(clientIP, now);
        if (now - lastRequestTime.get(clientIP) > WINDOW_MS) {
            requestCounts.put(clientIP, 0);
            lastRequestTime.put(clientIP, now);
        }
        requestCounts.put(clientIP, requestCounts.getOrDefault(clientIP, 0) + 1);
        if (requestCounts.get(clientIP) > MAX_REQUESTS) {
            // Too many requests in this window
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return;
        }
        filterChain.doFilter(request, response);
    }
}
