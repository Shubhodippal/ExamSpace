package com.example.demo.Security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${api.key}")
    private String apiKey;

    @Value("${api.header.name}")
    private String apiKeyHeaderName;

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request, 
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Skip API key validation for preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get API key from request header
        String requestApiKey = request.getHeader(apiKeyHeaderName);
        
        // Check if the API key is valid
        if (requestApiKey == null || !requestApiKey.equals(apiKey)) {
            // API key is invalid or missing
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\": \"Unauthorized: Invalid or missing API key\"}");
            return;
        }
        
        // API key is valid, continue with the request
        filterChain.doFilter(request, response);
    }
    
    /*@Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Optionally exclude certain paths from API key validation
        // For example, exclude user login/registration endpoints
        String path = request.getServletPath();
        return path.startsWith("/users/login") || 
               path.startsWith("/users/register"); ||
               path.startsWith("/users/verify");
    }*/
}