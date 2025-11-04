package com.kelox.backend.controller;

import com.kelox.backend.dto.CreateOfferRequest;
import com.kelox.backend.dto.OfferResponse;
import com.kelox.backend.dto.UpdateOfferRequest;
import com.kelox.backend.dto.UserOffersResponse;
import com.kelox.backend.enums.OfferStatus;
import com.kelox.backend.service.OfferService;
import com.kelox.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
@Slf4j
public class OfferController {
    
    private final OfferService offerService;
    private final JwtUtil jwtUtil;
    
    /**
     * Create a new offer
     * Requires: Authorization Bearer token
     * 
     * POST /api/offers
     * Body: CreateOfferRequest
     */
    @PostMapping
    public ResponseEntity<OfferResponse> createOffer(
            @RequestBody CreateOfferRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} creating new offer for hospital {}", userId, request.getHospitalId());
        
        OfferResponse offer = offerService.createOffer(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(offer);
    }
    
    /**
     * Get offer by ID
     * Requires: Authorization Bearer token
     * 
     * GET /api/offers/{offerId}
     */
    @GetMapping("/{offerId}")
    public ResponseEntity<OfferResponse> getOfferById(
            @PathVariable UUID offerId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Fetching offer with ID: {}", offerId);
        OfferResponse offer = offerService.getOfferById(offerId);
        
        return ResponseEntity.ok(offer);
    }
    
    /**
     * Get all offers created by the authenticated user
     * Requires: Authorization Bearer token
     * 
     * GET /api/offers/my-offers/created
     */
    @GetMapping("/my-offers/created")
    public ResponseEntity<List<OfferResponse>> getMyCreatedOffers(
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("Fetching offers created by user: {}", userId);
        
        List<OfferResponse> offers = offerService.getOffersByCreator(userId);
        log.info("Found {} offers created by user {}", offers.size(), userId);
        
        return ResponseEntity.ok(offers);
    }
    
    /**
     * Get all offers received by the authenticated user's hospital
     * Requires: Authorization Bearer token
     * User must own a hospital
     * 
     * GET /api/offers/my-offers/received
     */
    @GetMapping("/my-offers/received")
    public ResponseEntity<List<OfferResponse>> getMyReceivedOffers(
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("Fetching offers received by user's hospital: {}", userId);
        
        // This will internally fetch the hospital owned by the user and get its offers
        List<OfferResponse> offers = offerService.getOffersReceivedByUser(userId);
        log.info("Found {} offers received by user's hospital", offers.size());
        
        return ResponseEntity.ok(offers);
    }
    
    /**
     * Get all offers for the authenticated user
     * Requires: Authorization Bearer token
     * Returns both offers created by user and offers received by user's hospital
     * All statuses included, ordered by creation date descending
     * 
     * GET /api/offers/my-offers
     */
    @GetMapping("/my-offers")
    public ResponseEntity<UserOffersResponse> getAllMyOffers(
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("Fetching all offers for user: {}", userId);
        
        UserOffersResponse offers = offerService.getAllOffersForUser(userId);
        log.info("Found {} created offers and {} received offers for user {}", 
            offers.getCreatedOffers().size(), offers.getReceivedOffers().size(), userId);
        
        return ResponseEntity.ok(offers);
    }
    
    /**
     * Get offers by status for a specific hospital
     * Requires: Authorization Bearer token
     * 
     * GET /api/offers/hospitals/{hospitalId}?status={status}
     */
    @GetMapping("/hospitals/{hospitalId}")
    public ResponseEntity<List<OfferResponse>> getOffersByHospital(
            @PathVariable Long hospitalId,
            @RequestParam(required = false) OfferStatus status,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Fetching offers for hospital {} with status: {}", hospitalId, status);
        
        List<OfferResponse> offers;
        if (status != null) {
            offers = offerService.getOffersByHospitalAndStatus(hospitalId, status);
        } else {
            offers = offerService.getOffersByHospital(hospitalId);
        }
        
        log.info("Found {} offers for hospital {}", offers.size(), hospitalId);
        
        return ResponseEntity.ok(offers);
    }
    
    /**
     * Accept an offer
     * Requires: Authorization Bearer token
     * User must be the hospital owner
     * 
     * POST /api/offers/{offerId}/accept
     */
    @PostMapping("/{offerId}/accept")
    public ResponseEntity<OfferResponse> acceptOffer(
            @PathVariable UUID offerId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} accepting offer {}", userId, offerId);
        
        OfferResponse offer = offerService.acceptOffer(offerId, userId);
        
        return ResponseEntity.ok(offer);
    }
    
    /**
     * Reject an offer
     * Requires: Authorization Bearer token
     * User must be the hospital owner
     * 
     * POST /api/offers/{offerId}/reject
     */
    @PostMapping("/{offerId}/reject")
    public ResponseEntity<OfferResponse> rejectOffer(
            @PathVariable UUID offerId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} rejecting offer {}", userId, offerId);
        
        OfferResponse offer = offerService.rejectOffer(offerId, userId);
        
        return ResponseEntity.ok(offer);
    }
    
    /**
     * Update an offer
     * Requires: Authorization Bearer token
     * User must be the creator and offer must be pending
     * 
     * PUT /api/offers/{offerId}
     */
    @PutMapping("/{offerId}")
    public ResponseEntity<OfferResponse> updateOffer(
            @PathVariable UUID offerId,
            @RequestBody UpdateOfferRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} updating offer {}", userId, offerId);
        
        OfferResponse offer = offerService.updateOffer(offerId, request, userId);
        
        return ResponseEntity.ok(offer);
    }
    
    /**
     * Cancel an offer
     * Requires: Authorization Bearer token
     * User must be the creator and offer must be pending
     * Changes status to CANCELED instead of deleting
     * 
     * DELETE /api/offers/{offerId}
     */
    @DeleteMapping("/{offerId}")
    public ResponseEntity<OfferResponse> cancelOffer(
            @PathVariable UUID offerId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} canceling offer {}", userId, offerId);
        
        OfferResponse offer = offerService.cancelOffer(offerId, userId);
        
        return ResponseEntity.ok(offer);
    }
    
    /**
     * Get all offers by status
     * Requires: Authorization Bearer token
     * 
     * GET /api/offers/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OfferResponse>> getOffersByStatus(
            @PathVariable OfferStatus status,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Fetching offers with status: {}", status);
        
        List<OfferResponse> offers = offerService.getOffersByStatus(status);
        log.info("Found {} offers with status {}", offers.size(), status);
        
        return ResponseEntity.ok(offers);
    }
    
    /**
     * Check if authenticated user has a pending offer for a specific hospital
     * Requires: Authorization Bearer token
     * Returns the pending offer if exists, or 404 if no pending offer found
     * 
     * GET /api/offers/check-pending/hospitals/{hospitalId}
     */
    @GetMapping("/check-pending/hospitals/{hospitalId}")
    public ResponseEntity<OfferResponse> checkPendingOfferForHospital(
            @PathVariable Long hospitalId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("Checking for pending offer by user {} for hospital {}", userId, hospitalId);
        
        OfferResponse pendingOffer = offerService.getPendingOfferForUserAndHospital(userId, hospitalId);
        
        if (pendingOffer == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(pendingOffer);
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

