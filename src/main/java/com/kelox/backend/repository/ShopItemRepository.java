package com.kelox.backend.repository;

import com.kelox.backend.entity.ShopItem;
import com.kelox.backend.enums.ShopItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
    
    List<ShopItem> findByShoppingCartId(UUID shoppingCartId);
    
    Optional<ShopItem> findByShoppingCartIdAndProductId(UUID shoppingCartId, Long productId);
    
    boolean existsByShoppingCartIdAndProductId(UUID shoppingCartId, Long productId);
    
    List<ShopItem> findByOfferId(UUID offerId);
    
    Optional<ShopItem> findByShoppingCartIdAndProductIdAndType(UUID shoppingCartId, Long productId, ShopItemType type);
}

