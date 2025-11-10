package com.kelox.backend.dto;

import com.kelox.backend.entity.ShoppingCart;
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
public class ShoppingCartResponse {
    
    private UUID id;
    private Long hospitalId;
    private String hospitalName;
    private List<ShopItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalItems;
    private Float totalAmount;
    
    public static ShoppingCartResponse fromEntity(ShoppingCart cart) {
        ShoppingCartResponse response = new ShoppingCartResponse();
        response.setId(cart.getId());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        
        if (cart.getHospital() != null) {
            response.setHospitalId(cart.getHospital().getId());
            response.setHospitalName(cart.getHospital().getName());
        }
        
        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            List<ShopItemDto> itemDtos = cart.getItems().stream()
                .map(ShopItemDto::fromEntity)
                .collect(Collectors.toList());
            response.setItems(itemDtos);
            
            // Calculate totals
            response.setTotalItems(itemDtos.stream()
                .mapToInt(ShopItemDto::getQuantity)
                .sum());
            
            response.setTotalAmount(itemDtos.stream()
                .map(item -> item.getPrice() * item.getQuantity())
                .reduce(0f, Float::sum));
        } else {
            response.setItems(List.of());
            response.setTotalItems(0);
            response.setTotalAmount(0f);
        }
        
        return response;
    }
}

