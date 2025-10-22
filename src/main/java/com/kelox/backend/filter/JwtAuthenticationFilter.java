package com.kelox.backend.filter;

import com.kelox.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                // Validate and extract user info
                if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                    UUID userId = jwtUtil.getUserIdFromToken(token);
                    String wallet = jwtUtil.getWalletFromToken(token);
                    
                    // Store user info in request attributes for controllers to access
                    request.setAttribute("userId", userId);
                    request.setAttribute("wallet", wallet);
                    request.setAttribute("authenticated", true);
                    
                    log.debug("JWT validated for user: {}", userId);
                }
            } catch (Exception e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip JWT filter for public endpoints
        return path.startsWith("/api/health") || 
               path.startsWith("/api/auth/login") ||
               path.startsWith("/api/admin/");  // Admin uses different auth
    }
}

