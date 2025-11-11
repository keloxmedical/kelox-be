package com.kelox.backend.dto;

import com.kelox.backend.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesHistoryResponse {
    
    private UUID orderId;
    private Long buyerHospitalId;
    private String buyerHospitalName;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private OrderStatus status;
    private Boolean paid;
    private List<OrderItemDto> soldItems;  // Only items from the seller hospital
    private Float totalSalesAmount;  // Total for items from this hospital only
}

