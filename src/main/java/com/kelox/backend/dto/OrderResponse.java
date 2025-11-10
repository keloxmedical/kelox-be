package com.kelox.backend.dto;

import com.kelox.backend.entity.Order;
import com.kelox.backend.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private UUID id;
    private Long hospitalId;
    private String hospitalName;
    private DeliveryAddressDto deliveryAddress;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private OrderStatus status;
    private Float productsCost;
    private Float platformFee;
    private Float deliveryFee;
    private Float totalCost;
    private Boolean paid;
    
    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCreatedAt(order.getCreatedAt());
        response.setCompletedAt(order.getCompletedAt());
        response.setStatus(order.getStatus());
        response.setProductsCost(order.getProductsCost());
        response.setPlatformFee(order.getPlatformFee());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setTotalCost(order.getTotalCost());
        response.setPaid(order.getPaid());
        
        if (order.getHospital() != null) {
            response.setHospitalId(order.getHospital().getId());
            response.setHospitalName(order.getHospital().getName());
        }
        
        if (order.getDeliveryAddress() != null) {
            response.setDeliveryAddress(DeliveryAddressDto.fromEntity(order.getDeliveryAddress()));
        }
        
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            response.setItems(order.getOrderItems().stream()
                .map(OrderItemDto::fromEntity)
                .collect(Collectors.toList()));
        }
        
        return response;
    }
}

