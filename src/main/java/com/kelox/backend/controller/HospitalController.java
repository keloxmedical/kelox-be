package com.kelox.backend.controller;

import com.kelox.backend.dto.AddProductsRequest;
import com.kelox.backend.dto.CreateDeliveryAddressRequest;
import com.kelox.backend.dto.DeliveryAddressDto;
import com.kelox.backend.dto.HospitalProfileResponse;
import com.kelox.backend.dto.ProductResponse;
import com.kelox.backend.dto.UpdateDeliveryAddressRequest;
import com.kelox.backend.service.HospitalService;
import com.kelox.backend.service.ProductService;
import com.kelox.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/hospitals")
@RequiredArgsConstructor
@Slf4j
public class HospitalController {
    
    private final HospitalService hospitalService;
    private final ProductService productService;
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
     * Get hospital profile by name
     * Requires: Authorization Bearer token
     */
    @GetMapping("/by-name")
    public ResponseEntity<HospitalProfileResponse> getHospitalByName(
            @RequestParam String name,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract token from Authorization header
        String token = extractTokenFromHeader(authHeader);
        
        // Validate token
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("Fetching hospital profile by name: {}", name);
        
        // Get hospital profile by name
        HospitalProfileResponse hospitalProfile = hospitalService.getHospitalByName(name);
        
        return ResponseEntity.ok(hospitalProfile);
    }
    
    /**
     * Get all delivery addresses for a hospital
     * Requires: Authorization Bearer token
     * User must be the hospital owner
     * 
     * GET /api/hospitals/{hospitalId}/delivery-addresses
     */
    @GetMapping("/{hospitalId}/delivery-addresses")
    public ResponseEntity<List<DeliveryAddressDto>> getDeliveryAddresses(
            @PathVariable Long hospitalId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} fetching delivery addresses for hospital {}", userId, hospitalId);
        
        List<DeliveryAddressDto> addresses = hospitalService.getDeliveryAddresses(hospitalId, userId);
        
        return ResponseEntity.ok(addresses);
    }
    
    /**
     * Add delivery address to hospital
     * Requires: Authorization Bearer token
     * User must be the hospital owner
     * Maximum 5 addresses per hospital
     * 
     * POST /api/hospitals/{hospitalId}/delivery-addresses
     */
    @PostMapping("/{hospitalId}/delivery-addresses")
    public ResponseEntity<DeliveryAddressDto> addDeliveryAddress(
            @PathVariable Long hospitalId,
            @RequestBody CreateDeliveryAddressRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} adding delivery address for hospital {}", userId, hospitalId);
        
        DeliveryAddressDto address = hospitalService.addDeliveryAddress(hospitalId, request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }
    
    /**
     * Update delivery address
     * Requires: Authorization Bearer token
     * User must be the hospital owner
     * 
     * PUT /api/hospitals/{hospitalId}/delivery-addresses/{addressId}
     */
    @PutMapping("/{hospitalId}/delivery-addresses/{addressId}")
    public ResponseEntity<DeliveryAddressDto> updateDeliveryAddress(
            @PathVariable Long hospitalId,
            @PathVariable Long addressId,
            @RequestBody UpdateDeliveryAddressRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} updating delivery address {} for hospital {}", userId, addressId, hospitalId);
        
        DeliveryAddressDto address = hospitalService.updateDeliveryAddress(hospitalId, addressId, request, userId);
        
        return ResponseEntity.ok(address);
    }
    
    /**
     * Delete delivery address
     * Requires: Authorization Bearer token
     * User must be the hospital owner
     * 
     * DELETE /api/hospitals/{hospitalId}/delivery-addresses/{addressId}
     */
    @DeleteMapping("/{hospitalId}/delivery-addresses/{addressId}")
    public ResponseEntity<Void> deleteDeliveryAddress(
            @PathVariable Long hospitalId,
            @PathVariable Long addressId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} deleting delivery address {} for hospital {}", userId, addressId, hospitalId);
        
        hospitalService.deleteDeliveryAddress(hospitalId, addressId, userId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Add products to user's hospital
     * Requires: Authorization Bearer token
     * User must be the hospital owner
     * If product with same code + lot number exists: adds to quantity
     * If product with same code but different lot number: creates new product
     * 
     * POST /api/hospitals/my-products
     */
    @PostMapping("/my-products")
    public ResponseEntity<List<ProductResponse>> addProductsToMyHospital(
            @RequestBody AddProductsRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} adding products to their hospital", userId);
        
        // Find user's hospital
        HospitalProfileResponse hospital = hospitalService.getHospitalByOwnerId(userId);
        
        // Add products to the hospital
        List<ProductResponse> products = productService.addProductsForHospital(
            hospital.getId(), 
            request.getProducts()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(products);
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

