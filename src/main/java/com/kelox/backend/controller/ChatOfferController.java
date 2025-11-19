package com.kelox.backend.controller;

import com.kelox.backend.dto.OfferMessageResponse;
import com.kelox.backend.dto.SendOfferMessageRequest;
import com.kelox.backend.service.ChatOfferService;
import com.kelox.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/offers/{offerId}/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatOfferController {
    
    private final ChatOfferService chatOfferService;
    private final JwtUtil jwtUtil;
    
    /**
     * Send a message in an offer chat
     * Requires: Authorization Bearer token
     * User must be either the creator or the seller
     * Message max 255 characters
     * 
     * POST /api/offers/{offerId}/chat
     */
    @PostMapping
    public ResponseEntity<OfferMessageResponse> sendMessage(
            @PathVariable UUID offerId,
            @RequestBody SendOfferMessageRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} sending message to offer {}", userId, offerId);
        
        OfferMessageResponse message = chatOfferService.sendMessage(
            offerId, 
            request.getMessage(), 
            request.getType(),  // Can be null for normal messages, or REJECT
            userId
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
    
    /**
     * Get all messages for an offer
     * Requires: Authorization Bearer token
     * User must be either the creator or the seller
     * Returns messages ordered by creation time (oldest first)
     * 
     * GET /api/offers/{offerId}/chat
     */
    @GetMapping
    public ResponseEntity<List<OfferMessageResponse>> getMessages(
            @PathVariable UUID offerId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} fetching messages for offer {}", userId, offerId);
        
        List<OfferMessageResponse> messages = chatOfferService.getMessages(offerId, userId);
        
        return ResponseEntity.ok(messages);
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

