package com.kelox.backend.dto;

import com.kelox.backend.entity.OrderItem;
import com.kelox.backend.enums.ShopItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    
    private Long id;
    private ProductResponse product;
    private Integer quantity;
    private Float price;
    private ShopItemType type;
    private UUID offerId;
    
    public static OrderItemDto fromEntity(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setType(orderItem.getType());
        dto.setOfferId(orderItem.getOfferId());
        
        if (orderItem.getProduct() != null) {
            dto.setProduct(ProductResponse.fromEntity(orderItem.getProduct()));
        }
        
        return dto;
    }
}

