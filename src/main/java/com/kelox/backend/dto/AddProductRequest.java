package com.kelox.backend.dto;

import com.kelox.backend.enums.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductRequest {
    
    private String name;
    private String manufacturer;
    private String code;
    private String lotNumber;
    private LocalDateTime expiryDate;
    private String description;
    private Float price;
    private Integer quantity;
    private Unit unit;
}

