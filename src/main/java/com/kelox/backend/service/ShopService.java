package com.kelox.backend.service;

import com.kelox.backend.dto.ShoppingCartResponse;
import com.kelox.backend.entity.HospitalProfile;
import com.kelox.backend.entity.Offer;
import com.kelox.backend.entity.OfferProduct;
import com.kelox.backend.entity.Product;
import com.kelox.backend.entity.ShopItem;
import com.kelox.backend.entity.ShoppingCart;
import com.kelox.backend.enums.ShopItemType;
import com.kelox.backend.exception.BusinessException;
import com.kelox.backend.exception.ResourceNotFoundException;
import com.kelox.backend.repository.HospitalProfileRepository;
import com.kelox.backend.repository.ShopItemRepository;
import com.kelox.backend.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopService {
    
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShopItemRepository shopItemRepository;
    private final HospitalProfileRepository hospitalProfileRepository;
    
    /**
     * Add all products from an accepted offer to the creator's hospital shopping cart
     * When an offer is accepted, the products go to the buyer (creator's hospital)
     */
    @Transactional
    public void addOfferProductsToShoppingCart(Offer offer) {
        // Get the creator's hospital (the buyer)
        HospitalProfile creatorHospital = offer.getCreator().getHospitalProfile();
        
        if (creatorHospital == null) {
            throw new BusinessException(
                "Creator does not own a hospital. Cannot add products to shopping cart.");
        }
        
        log.info("Adding {} products from offer {} to shopping cart for creator's hospital {}", 
            offer.getOfferProducts().size(), offer.getId(), creatorHospital.getId());
        
        // Get or create shopping cart for the creator's hospital (buyer)
        ShoppingCart shoppingCart = shoppingCartRepository.findByHospitalId(creatorHospital.getId())
            .orElseGet(() -> {
                log.warn("Shopping cart not found for hospital {}, creating new one", creatorHospital.getId());
                ShoppingCart newCart = new ShoppingCart();
                newCart.setHospital(creatorHospital);
                return shoppingCartRepository.save(newCart);
            });
        
        // Add each offer product to the shopping cart
        // Always create new items for offers (each offer is tracked separately)
        for (OfferProduct offerProduct : offer.getOfferProducts()) {
            ShopItem newItem = new ShopItem();
            newItem.setShoppingCart(shoppingCart);
            newItem.setProduct(offerProduct.getProduct());
            newItem.setQuantity(offerProduct.getQuantity());
            newItem.setPrice(offerProduct.getPrice());
            newItem.setType(ShopItemType.OFFER);
            newItem.setOffer(offer);
            shopItemRepository.save(newItem);
            log.info("Added new OFFER shop item for product {} to cart (qty: {}, offerId: {})", 
                offerProduct.getProduct().getId(), newItem.getQuantity(), offer.getId());
        }
        
        log.info("Successfully added all products from offer {} to creator's hospital {} shopping cart", 
            offer.getId(), creatorHospital.getId());
    }
    
    /**
     * Get shopping cart for a hospital
     * Verifies that the user is the owner of the hospital
     */
    @Transactional(readOnly = true)
    public ShoppingCartResponse getShoppingCartForHospital(Long hospitalId, UUID userId) {
        log.info("Fetching shopping cart for hospital {} by user {}", hospitalId, userId);
        
        // Find the hospital
        HospitalProfile hospital = hospitalProfileRepository.findById(hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Hospital profile not found with ID: " + hospitalId));
        
        // Verify the user is the hospital owner
        if (hospital.getOwner() == null || !hospital.getOwner().getId().equals(userId)) {
            throw new BusinessException(
                "User is not authorized to access this hospital's shopping cart");
        }
        
        // Get shopping cart for the hospital
        ShoppingCart shoppingCart = shoppingCartRepository.findByHospitalId(hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Shopping cart not found for hospital ID: " + hospitalId));
        
        return ShoppingCartResponse.fromEntity(shoppingCart);
    }
    
    /**
     * Add a single product to shopping cart
     * Creates a SINGLE type shop item
     */
    @Transactional
    public ShoppingCartResponse addProductToCart(Long hospitalId, Product product, Integer quantity, UUID userId) {
        log.info("Adding product {} (qty: {}) to cart for hospital {} by user {}", product.getId(), quantity, hospitalId, userId);
        
        // Find the hospital
        HospitalProfile hospital = hospitalProfileRepository.findById(hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Hospital profile not found with ID: " + hospitalId));
        
        // Verify the user is the hospital owner
        if (hospital.getOwner() == null || !hospital.getOwner().getId().equals(userId)) {
            throw new BusinessException(
                "User is not authorized to manage this hospital's shopping cart");
        }
        
        // Get or create shopping cart for the hospital
        ShoppingCart shoppingCart = shoppingCartRepository.findByHospitalId(hospitalId)
            .orElseGet(() -> {
                log.warn("Shopping cart not found for hospital {}, creating new one", hospitalId);
                ShoppingCart newCart = new ShoppingCart();
                newCart.setHospital(hospital);
                return shoppingCartRepository.save(newCart);
            });
        
        // Check if a SINGLE type item already exists for this product
        Optional<ShopItem> existingItem = shopItemRepository.findByShoppingCartIdAndProductIdAndType(
            shoppingCart.getId(), 
            product.getId(),
            ShopItemType.SINGLE
        );
        
        if (existingItem.isPresent()) {
            // Update existing SINGLE item: add quantity
            ShopItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPrice(product.getPrice()); // Update to current price
            shopItemRepository.save(item);
            log.info("Updated existing SINGLE shop item for product {} in cart (new qty: {})", product.getId(), item.getQuantity());
        } else {
            // Create new SINGLE shop item (even if OFFER items exist for same product)
            ShopItem newItem = new ShopItem();
            newItem.setShoppingCart(shoppingCart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice());
            newItem.setType(ShopItemType.SINGLE);
            newItem.setOffer(null);
            shopItemRepository.save(newItem);
            log.info("Added new SINGLE shop item for product {} to cart (qty: {})", product.getId(), quantity);
        }
        
        // Return updated cart
        return ShoppingCartResponse.fromEntity(shoppingCart);
    }
    
    /**
     * Remove item(s) from shopping cart
     * SINGLE type: removes only the specified item
     * OFFER type: removes ALL items with the same offerId
     */
    @Transactional
    public void removeItemFromCart(Long hospitalId, Long itemId, UUID userId) {
        log.info("Removing item {} from cart for hospital {} by user {}", itemId, hospitalId, userId);
        
        // Find the hospital
        HospitalProfile hospital = hospitalProfileRepository.findById(hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Hospital profile not found with ID: " + hospitalId));
        
        // Verify the user is the hospital owner
        if (hospital.getOwner() == null || !hospital.getOwner().getId().equals(userId)) {
            throw new BusinessException(
                "User is not authorized to manage this hospital's shopping cart");
        }
        
        // Find the shop item
        ShopItem shopItem = shopItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Shop item not found with ID: " + itemId));
        
        // Verify the item belongs to the hospital's cart
        if (!shopItem.getShoppingCart().getHospital().getId().equals(hospitalId)) {
            throw new BusinessException(
                "Shop item does not belong to this hospital's shopping cart");
        }
        
        // Check type and remove accordingly
        if (shopItem.getType() == ShopItemType.SINGLE) {
            // Remove only this item
            shopItemRepository.delete(shopItem);
            log.info("Removed SINGLE shop item {} from cart", itemId);
        } else if (shopItem.getType() == ShopItemType.OFFER) {
            // Remove all items from the same offer
            if (shopItem.getOffer() == null) {
                throw new BusinessException("Shop item of type OFFER must have an associated offer");
            }
            
            UUID offerId = shopItem.getOffer().getId();
            List<ShopItem> offerItems = shopItemRepository.findByOfferId(offerId);
            shopItemRepository.deleteAll(offerItems);
            log.info("Removed {} OFFER items (offerId: {}) from cart", offerItems.size(), offerId);
        }
    }
}

