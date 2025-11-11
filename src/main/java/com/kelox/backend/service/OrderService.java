package com.kelox.backend.service;

import com.kelox.backend.dto.OrderResponse;
import com.kelox.backend.entity.Order;
import com.kelox.backend.entity.OrderItem;
import com.kelox.backend.entity.Product;
import com.kelox.backend.enums.OrderStatus;
import com.kelox.backend.exception.BusinessException;
import com.kelox.backend.exception.ResourceNotFoundException;
import com.kelox.backend.repository.OrderRepository;
import com.kelox.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    
    /**
     * Update order status
     * Admin only
     * When changing to CONFIRMING_PAYMENT, delivery fee must be provided
     */
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus, Float deliveryFee) {
        log.info("Admin updating order {} to status {}", orderId, newStatus);
        
        // Find the order
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Order not found with ID: " + orderId));
        
        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);
        
        // If changing to CONFIRMING_PAYMENT, delivery fee must be provided
        if (newStatus == OrderStatus.CONFIRMING_PAYMENT) {
            if (deliveryFee == null) {
                throw new BusinessException(
                    "Delivery fee is required when changing status to CONFIRMING_PAYMENT");
            }
            if (deliveryFee < 0) {
                throw new BusinessException("Delivery fee must be non-negative");
            }
            order.setDeliveryFee(deliveryFee);
            log.info("Delivery fee set to {} for order {}", deliveryFee, orderId);
        }
        
        // Update status
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order {} status updated to {}", orderId, newStatus);
        
        return OrderResponse.fromEntity(updatedOrder);
    }
    
    /**
     * Validate status transition
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Can't change from terminal states
        if (currentStatus == OrderStatus.COMPLETED) {
            throw new BusinessException("Cannot change status of a completed order");
        }
        if (currentStatus == OrderStatus.CANCELED) {
            throw new BusinessException("Cannot change status of a canceled order");
        }
        
        // Validate logical flow
        if (currentStatus == OrderStatus.CALCULATING_LOGISTICS && 
            newStatus != OrderStatus.CONFIRMING_PAYMENT && 
            newStatus != OrderStatus.CANCELED) {
            throw new BusinessException(
                "From CALCULATING_LOGISTICS can only move to CONFIRMING_PAYMENT or CANCELED");
        }
        
        if (currentStatus == OrderStatus.CONFIRMING_PAYMENT && 
            newStatus != OrderStatus.IN_TRANSIT && 
            newStatus != OrderStatus.CANCELED) {
            throw new BusinessException(
                "From CONFIRMING_PAYMENT can only move to IN_TRANSIT or CANCELED");
        }
        
        if (currentStatus == OrderStatus.IN_TRANSIT && 
            newStatus != OrderStatus.COMPLETED && 
            newStatus != OrderStatus.CANCELED) {
            throw new BusinessException(
                "From IN_TRANSIT can only move to COMPLETED or CANCELED");
        }
    }
    
    /**
     * Update order paid status
     * Admin only
     * When setting paid=true, reduces product quantities from inventory
     */
    @Transactional
    public OrderResponse updatePaidStatus(UUID orderId, Boolean paid) {
        log.info("Admin updating order {} paid status to {}", orderId, paid);
        
        // Find the order
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Order not found with ID: " + orderId));
        
        // If setting to paid=true and was previously false, reduce product quantities
        if (Boolean.TRUE.equals(paid) && !Boolean.TRUE.equals(order.getPaid())) {
            log.info("Order {} is being marked as paid, reducing product quantities", orderId);
            reduceProductQuantities(order);
        }
        
        // Update paid status
        order.setPaid(paid);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order {} paid status updated to {}", orderId, paid);
        
        return OrderResponse.fromEntity(updatedOrder);
    }
    
    /**
     * Reduce product quantities based on order items
     */
    private void reduceProductQuantities(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            
            if (product == null) {
                log.warn("OrderItem {} has no product, skipping quantity reduction", orderItem.getId());
                continue;
            }
            
            // Check if enough quantity is available
            if (product.getQuantity() < orderItem.getQuantity()) {
                throw new BusinessException(
                    "Insufficient quantity for product " + product.getName() + 
                    ". Available: " + product.getQuantity() + 
                    ", Required: " + orderItem.getQuantity());
            }
            
            // Reduce product quantity
            int newQuantity = product.getQuantity() - orderItem.getQuantity();
            product.setQuantity(newQuantity);
            productRepository.save(product);
            
            log.info("Reduced product {} quantity by {} (new quantity: {})", 
                product.getId(), orderItem.getQuantity(), newQuantity);
        }
        
        log.info("Successfully reduced quantities for all products in order {}", order.getId());
    }
}

