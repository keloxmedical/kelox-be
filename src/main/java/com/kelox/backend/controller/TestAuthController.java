package com.kelox.backend.controller;

import com.kelox.backend.dto.AuthResponse;
import com.kelox.backend.entity.User;
import com.kelox.backend.repository.UserRepository;
import com.kelox.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for authentication - ONLY FOR DEVELOPMENT
 * Remove or disable in production
 */
@RestController
@RequestMapping("/api/test/auth")
@RequiredArgsConstructor
@Slf4j
@Profile("!prod")  // Only active when NOT in production profile
public class TestAuthController {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    /**
     * Generate JWT token for existing user by Solana wallet address
     * FOR TESTING ONLY - bypasses signature verification
     */
    @PostMapping("/generate-token/{solanaWallet}")
    public ResponseEntity<AuthResponse> generateTestToken(@PathVariable String solanaWallet) {
        log.warn("TEST ENDPOINT: Generating token for Solana wallet: {}", solanaWallet);
        
        User user = userRepository.findBySolanaWallet(solanaWallet)
                .orElseThrow(() -> new RuntimeException("User not found with Solana wallet: " + solanaWallet));
        
        String token = jwtUtil.generateToken(user.getId(), user.getSolanaWallet());
        
        AuthResponse response = new AuthResponse(
            token,
            user.getId(),
            user.getPrivyId(),
            user.getEmail(),
            user.getSolanaWallet(),
            user.getEthereumWallet(),
            false,
            null  // Test endpoint doesn't include hospital
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Validate a JWT token
     * FOR TESTING ONLY
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = jwtUtil.validateToken(token);
            boolean isExpired = jwtUtil.isTokenExpired(token);
            
            response.put("valid", isValid);
            response.put("expired", isExpired);
            
            if (isValid && !isExpired) {
                response.put("userId", jwtUtil.getUserIdFromToken(token));
                response.put("wallet", jwtUtil.getWalletFromToken(token));
            }
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get test signature instructions
     */
    @GetMapping("/instructions")
    public ResponseEntity<Map<String, Object>> getInstructions() {
        Map<String, Object> instructions = new HashMap<>();
        
        instructions.put("step1", "Create a test user via admin API");
        instructions.put("step2", "Generate a test token using /test/auth/generate-token/{wallet}");
        instructions.put("step3", "Use the token in Authorization header: Bearer {token}");
        instructions.put("note", "This endpoint bypasses Solana signature verification - for testing only!");
        
        Map<String, String> example = new HashMap<>();
        example.put("createUser", "POST /api/admin/users with wallet address");
        example.put("generateToken", "POST /api/test/auth/generate-token/YOUR_WALLET_ADDRESS");
        example.put("useToken", "Add header: Authorization: Bearer YOUR_TOKEN");
        
        instructions.put("example", example);
        
        return ResponseEntity.ok(instructions);
    }
}

