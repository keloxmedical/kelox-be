package com.kelox.backend.controller;

import com.kelox.backend.dto.HospitalProfileResponse;
import com.kelox.backend.service.HospitalService;
import com.kelox.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/hospitals")
@RequiredArgsConstructor
@Slf4j
public class HospitalController {
    
    private final HospitalService hospitalService;
    private final JwtUtil jwtUtil;
    
    /**
     * Get hospital profile for the authenticated user
     * Requires: Authorization Bearer token
     */
    @GetMapping("/my-profile")
    public ResponseEntity<HospitalProfileResponse> getMyHospitalProfile(
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract token from Authorization header
        String token = extractTokenFromHeader(authHeader);
        
        // Validate and extract user ID from token
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(401).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("Fetching hospital profile for user: {}", userId);
        
        // Get hospital profile by owner ID
        HospitalProfileResponse hospitalProfile = hospitalService.getHospitalByOwnerId(userId);
        
        return ResponseEntity.ok(hospitalProfile);
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

