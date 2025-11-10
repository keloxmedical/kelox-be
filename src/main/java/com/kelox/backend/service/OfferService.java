package com.kelox.backend.service;

import com.kelox.backend.dto.CreateOfferRequest;
import com.kelox.backend.dto.OfferProductDto;
import com.kelox.backend.dto.OfferResponse;
import com.kelox.backend.dto.UpdateOfferRequest;
import com.kelox.backend.dto.UserOffersResponse;
import com.kelox.backend.entity.HospitalProfile;
import com.kelox.backend.entity.Offer;
import com.kelox.backend.entity.OfferProduct;
import com.kelox.backend.entity.Product;
import com.kelox.backend.entity.User;
import com.kelox.backend.enums.OfferStatus;
import com.kelox.backend.exception.BusinessException;
import com.kelox.backend.exception.ResourceNotFoundException;
import com.kelox.backend.repository.HospitalProfileRepository;
import com.kelox.backend.repository.OfferRepository;
import com.kelox.backend.repository.ProductRepository;
import com.kelox.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {
    
    private final OfferRepository offerRepository;
    private final HospitalProfileRepository hospitalProfileRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ShopService shopService;
    
    /**
     * Create a new offer
     */
    @Transactional
    public OfferResponse createOffer(CreateOfferRequest request, UUID creatorId) {
        log.info("Creating new offer for hospital {} by user {}", request.getHospitalId(), creatorId);
        
        // Validate request
        validateCreateOfferRequest(request);
        
        // Check if user already has a pending offer for this hospital
        if (offerRepository.existsByCreatorIdAndHospitalIdAndStatus(
                creatorId, request.getHospitalId(), OfferStatus.PENDING)) {
            throw new BusinessException(
                "You already have a pending offer for this hospital. " +
                "Please wait for it to be accepted or rejected, or delete it before creating a new one.");
        }
        
        // Find the hospital
        HospitalProfile hospital = hospitalProfileRepository.findById(request.getHospitalId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Hospital profile not found with ID: " + request.getHospitalId()));
        
        // Find the creator
        User creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "User not found with ID: " + creatorId));
        
        // Create the offer
        Offer offer = new Offer();
        offer.setHospital(hospital);
        offer.setCreator(creator);
        offer.setCreatedAt(LocalDateTime.now());
        offer.setStatus(OfferStatus.PENDING);
        
        // Add offer products
        for (OfferProductDto productDto : request.getProducts()) {
            // Validate product exists
            Product product = productRepository.findById(productDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found with ID: " + productDto.getProductId()));
            
            // Validate quantity and price
            if (productDto.getQuantity() == null || productDto.getQuantity() <= 0) {
                throw new BusinessException("Quantity must be greater than 0 for product: " + product.getName());
            }
            
            if (productDto.getPrice() == null || productDto.getPrice() < 0) {
                throw new BusinessException("Price must be non-negative for product: " + product.getName());
            }
            
            // Check if requested quantity is available
            if (productDto.getQuantity() > product.getQuantity()) {
                throw new BusinessException(
                    "Requested quantity (" + productDto.getQuantity() + 
                    ") exceeds available quantity (" + product.getQuantity() + 
                    ") for product: " + product.getName());
            }
            
            OfferProduct offerProduct = new OfferProduct();
            offerProduct.setProduct(product);
            offerProduct.setQuantity(productDto.getQuantity());
            offerProduct.setPrice(productDto.getPrice());
            
            offer.addOfferProduct(offerProduct);
        }
        
        Offer savedOffer = offerRepository.save(offer);
        log.info("Offer created with ID: {}", savedOffer.getId());
        
        return OfferResponse.fromEntity(savedOffer);
    }
    
    /**
     * Get offer by ID
     */
    @Transactional(readOnly = true)
    public OfferResponse getOfferById(UUID offerId) {
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Offer not found with ID: " + offerId));
        
        return OfferResponse.fromEntity(offer);
    }
    
    /**
     * Get all offers for a hospital
     */
    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersByHospital(Long hospitalId) {
        // Verify hospital exists
        if (!hospitalProfileRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital profile not found with ID: " + hospitalId);
        }
        
        return offerRepository.findByHospitalId(hospitalId).stream()
            .map(OfferResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all offers created by a user
     */
    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersByCreator(UUID creatorId) {
        // Verify user exists
        if (!userRepository.existsById(creatorId)) {
            throw new ResourceNotFoundException("User not found with ID: " + creatorId);
        }
        
        return offerRepository.findByCreatorId(creatorId).stream()
            .map(OfferResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all offers received by a user's hospital
     */
    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersReceivedByUser(UUID userId) {
        // Find the hospital owned by the user
        HospitalProfile hospital = hospitalProfileRepository.findByOwnerId(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No hospital profile found for user ID: " + userId));
        
        return offerRepository.findByHospitalId(hospital.getId()).stream()
            .map(OfferResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get pending offer for a specific user and hospital
     * Returns the pending offer if exists, otherwise returns null
     */
    @Transactional(readOnly = true)
    public OfferResponse getPendingOfferForUserAndHospital(UUID userId, Long hospitalId) {
        log.info("Checking for pending offer by user {} for hospital {}", userId, hospitalId);
        
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        // Verify hospital exists
        if (!hospitalProfileRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital profile not found with ID: " + hospitalId);
        }
        
        return offerRepository.findByCreatorIdAndHospitalIdAndStatus(
                userId, hospitalId, OfferStatus.PENDING)
            .map(OfferResponse::fromEntity)
            .orElse(null);
    }
    
    /**
     * Get all offers for the authenticated user
     * Returns both offers created by the user and offers received by the user's hospital
     */
    @Transactional(readOnly = true)
    public UserOffersResponse getAllOffersForUser(UUID userId) {
        log.info("Fetching all offers for user: {}", userId);
        
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        // Get offers created by user (ordered by creation date desc)
        List<OfferResponse> createdOffers = offerRepository
            .findByCreatorIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(OfferResponse::fromEntity)
            .collect(Collectors.toList());
        
        // Get offers received by user's hospital (if user owns a hospital)
        List<OfferResponse> receivedOffers;
        Optional<HospitalProfile> hospitalOptional = hospitalProfileRepository.findByOwnerId(userId);
        
        if (hospitalOptional.isPresent()) {
            receivedOffers = offerRepository
                .findByHospitalIdOrderByCreatedAtDesc(hospitalOptional.get().getId())
                .stream()
                .map(OfferResponse::fromEntity)
                .collect(Collectors.toList());
        } else {
            receivedOffers = List.of(); // Empty list if user doesn't own a hospital
        }
        
        log.info("Found {} created offers and {} received offers for user {}", 
            createdOffers.size(), receivedOffers.size(), userId);
        
        return new UserOffersResponse(createdOffers, receivedOffers);
    }
    
    /**
     * Get offers by status
     */
    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersByStatus(OfferStatus status) {
        return offerRepository.findByStatus(status).stream()
            .map(OfferResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get offers for a hospital by status
     */
    @Transactional(readOnly = true)
    public List<OfferResponse> getOffersByHospitalAndStatus(Long hospitalId, OfferStatus status) {
        // Verify hospital exists
        if (!hospitalProfileRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital profile not found with ID: " + hospitalId);
        }
        
        return offerRepository.findByHospitalIdAndStatus(hospitalId, status).stream()
            .map(OfferResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Accept an offer
     * Automatically adds all offer products to the hospital's shopping cart
     */
    @Transactional
    public OfferResponse acceptOffer(UUID offerId, UUID userId) {
        log.info("Accepting offer {} by user {}", offerId, userId);
        
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Offer not found with ID: " + offerId));
        
        // Verify the user is the hospital owner
        verifyUserIsHospitalOwner(userId, offer.getHospital().getId());
        
        // Check if offer is in pending status
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BusinessException(
                "Cannot accept offer. Current status: " + offer.getStatus());
        }
        
        // Update offer status
        offer.setStatus(OfferStatus.ACCEPTED);
        Offer updatedOffer = offerRepository.save(offer);
        
        // Add all offer products to the hospital's shopping cart
        shopService.addOfferProductsToShoppingCart(offer);
        
        log.info("Offer {} accepted successfully and products added to shopping cart", offerId);
        
        return OfferResponse.fromEntity(updatedOffer);
    }
    
    /**
     * Reject an offer
     */
    @Transactional
    public OfferResponse rejectOffer(UUID offerId, UUID userId) {
        log.info("Rejecting offer {} by user {}", offerId, userId);
        
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Offer not found with ID: " + offerId));
        
        // Verify the user is the hospital owner
        verifyUserIsHospitalOwner(userId, offer.getHospital().getId());
        
        // Check if offer is in pending status
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BusinessException(
                "Cannot reject offer. Current status: " + offer.getStatus());
        }
        
        // Update offer status
        offer.setStatus(OfferStatus.REJECTED);
        Offer updatedOffer = offerRepository.save(offer);
        
        log.info("Offer {} rejected successfully", offerId);
        
        return OfferResponse.fromEntity(updatedOffer);
    }
    
    /**
     * Cancel an offer (only if pending and created by the user)
     * Changes status to CANCELED instead of deleting
     */
    @Transactional
    public OfferResponse cancelOffer(UUID offerId, UUID userId) {
        log.info("Canceling offer {} by user {}", offerId, userId);
        
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Offer not found with ID: " + offerId));
        
        // Verify the user is the creator
        if (!offer.getCreator().getId().equals(userId)) {
            throw new BusinessException(
                "Only the creator can cancel an offer");
        }
        
        // Check if offer is in pending status
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BusinessException(
                "Cannot cancel offer. Only pending offers can be canceled. Current status: " + offer.getStatus());
        }
        
        // Update status to CANCELED
        offer.setStatus(OfferStatus.CANCELED);
        Offer canceledOffer = offerRepository.save(offer);
        
        log.info("Offer {} canceled successfully", offerId);
        
        return OfferResponse.fromEntity(canceledOffer);
    }
    
    /**
     * Update an offer (only if pending and created by the user)
     */
    @Transactional
    public OfferResponse updateOffer(UUID offerId, UpdateOfferRequest request, UUID userId) {
        log.info("Updating offer {} by user {}", offerId, userId);
        
        // Validate request
        validateUpdateOfferRequest(request);
        
        // Find the offer
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Offer not found with ID: " + offerId));
        
        // Verify the user is the creator
        if (!offer.getCreator().getId().equals(userId)) {
            throw new BusinessException(
                "Only the creator can update an offer");
        }
        
        // Check if offer is in pending status
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BusinessException(
                "Cannot update offer. Only pending offers can be updated. Current status: " + offer.getStatus());
        }
        
        // Clear existing offer products
        offer.getOfferProducts().clear();
        
        // Add new offer products
        for (OfferProductDto productDto : request.getProducts()) {
            // Validate product exists
            Product product = productRepository.findById(productDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found with ID: " + productDto.getProductId()));
            
            // Validate quantity and price
            if (productDto.getQuantity() == null || productDto.getQuantity() <= 0) {
                throw new BusinessException("Quantity must be greater than 0 for product: " + product.getName());
            }
            
            if (productDto.getPrice() == null || productDto.getPrice() < 0) {
                throw new BusinessException("Price must be non-negative for product: " + product.getName());
            }
            
            // Check if requested quantity is available
            if (productDto.getQuantity() > product.getQuantity()) {
                throw new BusinessException(
                    "Requested quantity (" + productDto.getQuantity() + 
                    ") exceeds available quantity (" + product.getQuantity() + 
                    ") for product: " + product.getName());
            }
            
            OfferProduct offerProduct = new OfferProduct();
            offerProduct.setProduct(product);
            offerProduct.setQuantity(productDto.getQuantity());
            offerProduct.setPrice(productDto.getPrice());
            
            offer.addOfferProduct(offerProduct);
        }
        
        Offer updatedOffer = offerRepository.save(offer);
        log.info("Offer {} updated successfully", offerId);
        
        return OfferResponse.fromEntity(updatedOffer);
    }
    
    /**
     * Validate create offer request
     */
    private void validateCreateOfferRequest(CreateOfferRequest request) {
        if (request.getHospitalId() == null) {
            throw new BusinessException("Hospital ID is required");
        }
        
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new BusinessException("At least one product is required for an offer");
        }
        
        // Validate each product
        for (OfferProductDto product : request.getProducts()) {
            if (product.getProductId() == null) {
                throw new BusinessException("Product ID is required");
            }
            if (product.getQuantity() == null || product.getQuantity() <= 0) {
                throw new BusinessException("Quantity must be greater than 0");
            }
            if (product.getPrice() == null || product.getPrice() < 0) {
                throw new BusinessException("Price must be non-negative");
            }
        }
    }
    
    /**
     * Validate update offer request
     */
    private void validateUpdateOfferRequest(UpdateOfferRequest request) {
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new BusinessException("At least one product is required for an offer");
        }
        
        // Validate each product
        for (OfferProductDto product : request.getProducts()) {
            if (product.getProductId() == null) {
                throw new BusinessException("Product ID is required");
            }
            if (product.getQuantity() == null || product.getQuantity() <= 0) {
                throw new BusinessException("Quantity must be greater than 0");
            }
            if (product.getPrice() == null || product.getPrice() < 0) {
                throw new BusinessException("Price must be non-negative");
            }
        }
    }
    
    /**
     * Verify user is the hospital owner
     */
    private void verifyUserIsHospitalOwner(UUID userId, Long hospitalId) {
        HospitalProfile hospital = hospitalProfileRepository.findById(hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Hospital profile not found with ID: " + hospitalId));
        
        if (hospital.getOwner() == null || !hospital.getOwner().getId().equals(userId)) {
            throw new BusinessException(
                "User is not authorized to manage offers for this hospital");
        }
    }
}

