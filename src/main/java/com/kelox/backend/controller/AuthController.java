package com.kelox.backend.controller;

import com.kelox.backend.dto.AuthRequest;
import com.kelox.backend.dto.AuthResponse;
import com.kelox.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Authenticate user with Privy + Solana wallet signature
     * No authentication required - this is the login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        log.info("Login attempt for Privy ID: {}, Email: {}", request.getPrivyId(), request.getEmail());
        AuthResponse response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Refresh JWT token
     * Requires valid JWT token in Authorization header
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = extractTokenFromHeader(authHeader);
        AuthResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Validate token and get user info
     * Requires valid JWT token in Authorization header
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = extractTokenFromHeader(authHeader);
        AuthResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }
}

