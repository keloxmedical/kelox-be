package com.kelox.backend.controller;

import com.kelox.backend.dto.OrderResponse;
import com.kelox.backend.dto.UpdateOrderStatusRequest;
import com.kelox.backend.dto.UpdatePaidStatusRequest;
import com.kelox.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {
    
    private final OrderService orderService;
    
    /**
     * Update order status
     * Requires: X-Admin-Secret header
     * 
     * When changing to CONFIRMING_PAYMENT: deliveryFee is required
     * When changing to IN_TRANSIT, COMPLETED, CANCELED: deliveryFee is optional
     * 
     * PUT /api/admin/orders/{orderId}/status
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        
        log.info("Admin updating order {} to status {}", orderId, request.getStatus());
        
        OrderResponse order = orderService.updateOrderStatus(
            orderId, 
            request.getStatus(), 
            request.getDeliveryFee()
        );
        
        return ResponseEntity.ok(order);
    }
    
    /**
     * Update order paid status
     * Requires: X-Admin-Secret header
     * Admin only - can set paid to true or false
     * 
     * PUT /api/admin/orders/{orderId}/paid
     */
    @PutMapping("/{orderId}/paid")
    public ResponseEntity<OrderResponse> updatePaidStatus(
            @PathVariable UUID orderId,
            @RequestBody UpdatePaidStatusRequest request) {
        
        log.info("Admin updating order {} paid status to {}", orderId, request.getPaid());
        
        OrderResponse order = orderService.updatePaidStatus(orderId, request.getPaid());
        
        return ResponseEntity.ok(order);
    }
}

