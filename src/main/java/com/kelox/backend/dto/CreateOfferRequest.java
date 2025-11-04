package com.kelox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOfferRequest {
    
    private Long hospitalId;
    private List<OfferProductDto> products;
}

