package com.kelox.backend.dto;

import com.kelox.backend.entity.OfferProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferProductDto {
    
    private Long productId;
    private Integer quantity;
    private Float price;
    
    // Additional fields for response
    private String productName;
    private String productCode;
    
    public static OfferProductDto fromEntity(OfferProduct offerProduct) {
        OfferProductDto dto = new OfferProductDto();
        dto.setProductId(offerProduct.getProduct().getId());
        dto.setQuantity(offerProduct.getQuantity());
        dto.setPrice(offerProduct.getPrice());
        dto.setProductName(offerProduct.getProduct().getName());
        dto.setProductCode(offerProduct.getProduct().getCode());
        return dto;
    }
}

