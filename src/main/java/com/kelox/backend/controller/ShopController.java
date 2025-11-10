package com.kelox.backend.controller;

import com.kelox.backend.dto.AddToCartRequest;
import com.kelox.backend.dto.OrderResponse;
import com.kelox.backend.dto.RequestDeliveryPriceRequest;
import com.kelox.backend.dto.ShoppingCartResponse;

import java.util.List;
import com.kelox.backend.service.ProductService;
import com.kelox.backend.service.ShopService;
import com.kelox.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
@Slf4j
public class ShopController {
    
    private final ShopService shopService;
    private final ProductService productService;
    private final JwtUtil jwtUtil;
    
    /**
     * Add product to shopping cart
     * Requires: Authorization Bearer token
     * User must own a hospital
     * Creates a SINGLE type shop item
     * 
     * POST /api/shop/cart/add
     */
    @PostMapping("/cart/add")
    public ResponseEntity<ShoppingCartResponse> addProductToCart(
            @RequestBody AddToCartRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} adding product {} to cart", userId, request.getProductId());
        
        ShoppingCartResponse cart = productService.addToCart(request, userId);
        
        return ResponseEntity.ok(cart);
    }
    
    /**
     * Get shopping cart for a specific hospital
     * Requires: Authorization Bearer token
     * User must be the hospital owner
     * 
     * GET /api/shop/hospitals/{hospitalId}/cart
     */
    @GetMapping("/hospitals/{hospitalId}/cart")
    public ResponseEntity<ShoppingCartResponse> getShoppingCart(
            @PathVariable Long hospitalId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} fetching shopping cart for hospital {}", userId, hospitalId);
        
        ShoppingCartResponse cart = shopService.getShoppingCartForHospital(hospitalId, userId);
        
        return ResponseEntity.ok(cart);
    }
    
    /**
     * Remove item(s) from shopping cart
     * Requires: Authorization Bearer token
     * User must be the hospital owner
     * SINGLE type: removes only the specified item
     * OFFER type: removes ALL items with the same offerId
     * 
     * DELETE /api/shop/hospitals/{hospitalId}/cart/items/{itemId}
     */
    @DeleteMapping("/hospitals/{hospitalId}/cart/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long hospitalId,
            @PathVariable Long itemId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} removing item {} from cart for hospital {}", userId, itemId, hospitalId);
        
        shopService.removeItemFromCart(hospitalId, itemId, userId);
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Request delivery price - creates an order from shopping cart
     * Requires: Authorization Bearer token
     * User must own a hospital and have items in cart
     * Creates order with status CALCULATING_LOGISTICS
     * 
     * POST /api/shop/request-delivery-price
     */
    @PostMapping("/request-delivery-price")
    public ResponseEntity<OrderResponse> requestDeliveryPrice(
            @RequestBody RequestDeliveryPriceRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} requesting delivery price", userId);
        
        OrderResponse order = shopService.requestDeliveryPrice(request.getDeliveryAddressId(), userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    /**
     * Get all pending orders for authenticated user's hospital
     * Pending = all statuses except COMPLETED and CANCELED
     * Requires: Authorization Bearer token
     * User must own a hospital
     * 
     * GET /api/shop/orders/pending
     */
    @GetMapping("/orders/pending")
    public ResponseEntity<List<OrderResponse>> getPendingOrders(
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} fetching pending orders", userId);
        
        List<OrderResponse> orders = shopService.getPendingOrders(userId);
        log.info("Found {} pending orders for user {}", orders.size(), userId);
        
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Get all orders for authenticated user's hospital
     * Includes all statuses (COMPLETED, CANCELED, etc.)
     * Ordered by creation date descending
     * Requires: Authorization Bearer token
     * User must own a hospital
     * 
     * GET /api/shop/orders
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestHeader("Authorization") String authHeader) {
        
        // Extract and validate token
        String token = extractTokenFromHeader(authHeader);
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        UUID userId = jwtUtil.getUserIdFromToken(token);
        log.info("User {} fetching all orders", userId);
        
        List<OrderResponse> orders = shopService.getAllOrders(userId);
        log.info("Found {} total orders for user {}", orders.size(), userId);
        
        return ResponseEntity.ok(orders);
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

