package com.kelox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDeliveryPriceRequest {
    
    private Long deliveryAddressId;
}

