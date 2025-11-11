package com.kelox.backend.service;

import com.kelox.backend.dto.OrderItemDto;
import com.kelox.backend.dto.OrderResponse;
import com.kelox.backend.dto.SalesHistoryResponse;
import com.kelox.backend.dto.ShoppingCartResponse;
import com.kelox.backend.entity.DeliveryAddress;
import com.kelox.backend.entity.HospitalProfile;
import com.kelox.backend.entity.Offer;
import com.kelox.backend.entity.OfferProduct;
import com.kelox.backend.entity.Order;
import com.kelox.backend.entity.OrderItem;
import com.kelox.backend.entity.Product;
import com.kelox.backend.entity.ShopItem;
import com.kelox.backend.entity.ShoppingCart;
import com.kelox.backend.enums.OrderStatus;
import com.kelox.backend.enums.ShopItemType;
import com.kelox.backend.exception.BusinessException;
import com.kelox.backend.exception.ResourceNotFoundException;
import com.kelox.backend.repository.DeliveryAddressRepository;
import com.kelox.backend.repository.HospitalProfileRepository;
import com.kelox.backend.repository.OrderRepository;
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
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final OrderRepository orderRepository;
    
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
    
    /**
     * Request delivery price - creates an order from shopping cart
     * User must own a hospital and have items in cart
     * Status starts as CALCULATING_LOGISTICS
     */
    @Transactional
    public OrderResponse requestDeliveryPrice(Long deliveryAddressId, UUID userId) {
        log.info("User {} requesting delivery price for delivery address {}", userId, deliveryAddressId);
        
        // Find user's hospital
        HospitalProfile hospital = hospitalProfileRepository.findByOwnerId(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No hospital profile found for user ID: " + userId));
        
        // Get shopping cart
        ShoppingCart shoppingCart = shoppingCartRepository.findByHospitalId(hospital.getId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Shopping cart not found for hospital ID: " + hospital.getId()));
        
        // Validate cart has items
        if (shoppingCart.getItems() == null || shoppingCart.getItems().isEmpty()) {
            throw new BusinessException("Shopping cart is empty. Add items before creating an order.");
        }
        
        // Validate delivery address
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(deliveryAddressId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Delivery address not found with ID: " + deliveryAddressId));
        
        // Verify delivery address belongs to the hospital
        if (!deliveryAddress.getHospital().getId().equals(hospital.getId())) {
            throw new BusinessException(
                "Delivery address does not belong to this hospital");
        }
        
        // Calculate products cost
        Float productsCost = shoppingCart.getItems().stream()
            .map(item -> item.getPrice() * item.getQuantity())
            .reduce(0f, Float::sum);
        
        // Calculate platform fee (10% of products cost)
        Float platformFee = productsCost * 0.10f;
        
        // Create order
        Order order = new Order();
        order.setHospital(hospital);
        order.setDeliveryAddress(deliveryAddress);
        order.setStatus(OrderStatus.CALCULATING_LOGISTICS);
        order.setProductsCost(productsCost);
        order.setPlatformFee(platformFee);
        order.setDeliveryFee(null);  // Will be calculated by logistics system later
        
        // Map shop items to order items
        for (ShopItem shopItem : shoppingCart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(shopItem.getProduct());
            orderItem.setQuantity(shopItem.getQuantity());
            orderItem.setPrice(shopItem.getPrice());
            orderItem.setType(shopItem.getType());
            
            if (shopItem.getOffer() != null) {
                orderItem.setOfferId(shopItem.getOffer().getId());
            }
            
            order.addOrderItem(orderItem);
        }
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {} for hospital {}", savedOrder.getId(), hospital.getId());
        
        // Clear shopping cart
        shoppingCart.clearItems();
        shoppingCartRepository.save(shoppingCart);
        log.info("Shopping cart cleared for hospital {}", hospital.getId());
        
        return OrderResponse.fromEntity(savedOrder);
    }
    
    /**
     * Get all pending orders for user's hospital
     * Pending = all statuses except COMPLETED and CANCELED
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getPendingOrders(UUID userId) {
        log.info("User {} fetching pending orders", userId);
        
        // Find user's hospital
        HospitalProfile hospital = hospitalProfileRepository.findByOwnerId(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No hospital profile found for user ID: " + userId));
        
        // Get pending orders
        List<Order> pendingOrders = orderRepository.findPendingOrdersByHospitalId(hospital.getId());
        log.info("Found {} pending orders for hospital {}", pendingOrders.size(), hospital.getId());
        
        return pendingOrders.stream()
            .map(OrderResponse::fromEntity)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get all orders for user's hospital
     * Includes all statuses, ordered by creation date descending
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(UUID userId) {
        log.info("User {} fetching all orders", userId);
        
        // Find user's hospital
        HospitalProfile hospital = hospitalProfileRepository.findByOwnerId(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No hospital profile found for user ID: " + userId));
        
        // Get all orders
        List<Order> orders = orderRepository.findByHospitalIdOrderByCreatedAtDesc(hospital.getId());
        log.info("Found {} total orders for hospital {}", orders.size(), hospital.getId());
        
        return orders.stream()
            .map(OrderResponse::fromEntity)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get sales history for user's hospital (as seller)
     * Shows orders where products from this hospital were sold
     * Includes: IN_TRANSIT, COMPLETED, CONFIRMING_PAYMENT (if paid=true)
     * Only shows items from the seller hospital in each order
     */
    @Transactional(readOnly = true)
    public List<SalesHistoryResponse> getSalesHistory(UUID userId) {
        log.info("User {} fetching sales history", userId);
        
        // Find user's hospital
        HospitalProfile hospital = hospitalProfileRepository.findByOwnerId(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No hospital profile found for user ID: " + userId));
        
        // Get orders containing products from this hospital (as seller)
        List<Order> orders = orderRepository.findSalesHistoryBySellerHospitalId(hospital.getId());
        log.info("Found {} orders in sales history for hospital {}", orders.size(), hospital.getId());
        
        // Map to sales history response
        return orders.stream()
            .map(order -> {
                SalesHistoryResponse response = new SalesHistoryResponse();
                response.setOrderId(order.getId());
                response.setCreatedAt(order.getCreatedAt());
                response.setCompletedAt(order.getCompletedAt());
                response.setStatus(order.getStatus());
                response.setPaid(order.getPaid());
                
                if (order.getHospital() != null) {
                    response.setBuyerHospitalId(order.getHospital().getId());
                    response.setBuyerHospitalName(order.getHospital().getName());
                }
                
                // Filter order items to only include products from the seller hospital
                List<OrderItemDto> soldItems = order.getOrderItems().stream()
                    .filter(item -> item.getProduct() != null && 
                                   item.getProduct().getSeller() != null &&
                                   item.getProduct().getSeller().getId().equals(hospital.getId()))
                    .map(OrderItemDto::fromEntity)
                    .collect(java.util.stream.Collectors.toList());
                
                response.setSoldItems(soldItems);
                
                // Calculate total sales amount for this hospital's items only
                Float totalSales = soldItems.stream()
                    .map(item -> item.getPrice() * item.getQuantity())
                    .reduce(0f, Float::sum);
                response.setTotalSalesAmount(totalSales);
                
                return response;
            })
            .collect(java.util.stream.Collectors.toList());
    }
}

