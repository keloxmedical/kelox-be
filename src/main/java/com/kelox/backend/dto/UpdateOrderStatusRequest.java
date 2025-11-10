package com.kelox.backend.dto;

import com.kelox.backend.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    
    private OrderStatus status;
    private Float deliveryFee;  // Optional: only required when changing to CONFIRMING_PAYMENT
}

