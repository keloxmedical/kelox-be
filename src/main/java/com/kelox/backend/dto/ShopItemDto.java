package com.kelox.backend.dto;

import com.kelox.backend.entity.ShopItem;
import com.kelox.backend.enums.ShopItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopItemDto {
    
    private Long id;
    private ProductResponse product;
    private Integer quantity;
    private Float price;
    private ShopItemType type;
    private UUID offerId;
    
    public static ShopItemDto fromEntity(ShopItem shopItem) {
        ShopItemDto dto = new ShopItemDto();
        dto.setId(shopItem.getId());
        dto.setQuantity(shopItem.getQuantity());
        dto.setPrice(shopItem.getPrice());
        dto.setType(shopItem.getType());
        
        if (shopItem.getProduct() != null) {
            dto.setProduct(ProductResponse.fromEntity(shopItem.getProduct()));
        }
        
        if (shopItem.getOffer() != null) {
            dto.setOfferId(shopItem.getOffer().getId());
        }
        
        return dto;
    }
}

