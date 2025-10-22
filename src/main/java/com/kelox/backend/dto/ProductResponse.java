package com.kelox.backend.dto;

import com.kelox.backend.entity.Product;
import com.kelox.backend.enums.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private Long id;
    private String name;
    private String manufacturer;
    private String code;
    private String lotNumber;
    private LocalDateTime expiryDate;
    private String description;
    private Float price;
    private Integer quantity;
    private Unit unit;
    private Long sellerHospitalId;
    private String sellerHospitalName;
    
    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setManufacturer(product.getManufacturer());
        response.setCode(product.getCode());
        response.setLotNumber(product.getLotNumber());
        response.setExpiryDate(product.getExpiryDate());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setQuantity(product.getQuantity());
        response.setUnit(product.getUnit());
        
        if (product.getSeller() != null) {
            response.setSellerHospitalId(product.getSeller().getId());
            response.setSellerHospitalName(product.getSeller().getName());
        }
        
        return response;
    }
}

