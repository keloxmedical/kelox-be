package com.kelox.backend.service;

import com.kelox.backend.dto.AddProductRequest;
import com.kelox.backend.dto.AddToCartRequest;
import com.kelox.backend.dto.ProductResponse;
import com.kelox.backend.dto.ShoppingCartResponse;
import com.kelox.backend.entity.HospitalProfile;
import com.kelox.backend.entity.Product;
import com.kelox.backend.exception.BusinessException;
import com.kelox.backend.exception.ResourceNotFoundException;
import com.kelox.backend.repository.HospitalProfileRepository;
import com.kelox.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final HospitalProfileRepository hospitalProfileRepository;
    private final ShopService shopService;
    
    /**
     * Add a list of products for a hospital
     * If product with same code + lot number exists: updates quantity (adds to existing)
     * If product with same code but different lot number: creates new product
     */
    @Transactional
    public List<ProductResponse> addProductsForHospital(Long hospitalId, List<AddProductRequest> productRequests) {
        log.info("Adding {} products for hospital ID: {}", productRequests.size(), hospitalId);
        
        // Validate hospital exists
        HospitalProfile hospital = hospitalProfileRepository.findById(hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Hospital profile not found with ID: " + hospitalId));
        
        List<Product> processedProducts = new java.util.ArrayList<>();
        
        // Process each product request
        for (AddProductRequest request : productRequests) {
            validateProductRequest(request);
            
            // Check if product with same code + lot number exists for this hospital
            java.util.Optional<Product> existingProduct = productRepository.findByCodeAndLotNumberAndSellerId(
                request.getCode(), request.getLotNumber(), hospitalId);
            
            if (existingProduct.isPresent()) {
                // Update existing product: add to quantity, don't update price
                Product product = existingProduct.get();
                int newQuantity = product.getQuantity() + request.getQuantity();
                product.setQuantity(newQuantity);
                
                // Update other fields that might have changed (except price)
                product.setName(request.getName());
                product.setManufacturer(request.getManufacturer());
                product.setExpiryDate(request.getExpiryDate());
                product.setDescription(request.getDescription());
                product.setUnit(request.getUnit());
                
                Product savedProduct = productRepository.save(product);
                processedProducts.add(savedProduct);
                log.info("Updated existing product {} (code: {}, lot: {}), added {} to quantity (new total: {})",
                    product.getId(), request.getCode(), request.getLotNumber(), 
                    request.getQuantity(), newQuantity);
            } else {
                // Create new product (same code but different lot number is OK)
                Product product = new Product();
                product.setName(request.getName());
                product.setManufacturer(request.getManufacturer());
                product.setCode(request.getCode());
                product.setLotNumber(request.getLotNumber());
                product.setExpiryDate(request.getExpiryDate());
                product.setDescription(request.getDescription());
                product.setPrice(request.getPrice());
                product.setQuantity(request.getQuantity());
                product.setUnit(request.getUnit());
                product.setSeller(hospital);
                
                Product savedProduct = productRepository.save(product);
                processedProducts.add(savedProduct);
                log.info("Created new product (code: {}, lot: {}, qty: {})",
                    request.getCode(), request.getLotNumber(), request.getQuantity());
            }
        }
        
        log.info("Successfully processed {} products for hospital ID: {}", processedProducts.size(), hospitalId);
        
        return processedProducts.stream()
            .map(ProductResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all products for a hospital
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByHospital(Long hospitalId) {
        // Verify hospital exists
        if (!hospitalProfileRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital profile not found with ID: " + hospitalId);
        }
        
        return productRepository.findBySellerIdOrderByExpiryDateAsc(hospitalId).stream()
            .map(ProductResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with ID: " + productId));
        
        return ProductResponse.fromEntity(product);
    }
    
    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
            .map(ProductResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Validate product request
     */
    private void validateProductRequest(AddProductRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException("Product name is required");
        }
        
        if (request.getManufacturer() == null || request.getManufacturer().trim().isEmpty()) {
            throw new BusinessException("Manufacturer is required");
        }
        
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BusinessException("Product code is required");
        }
        
        if (request.getLotNumber() == null || request.getLotNumber().trim().isEmpty()) {
            throw new BusinessException("Lot number is required");
        }
        
        if (request.getExpiryDate() == null) {
            throw new BusinessException("Expiry date is required");
        }
        
        // Validate expiry date is in the future
        if (request.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Expiry date must be in the future");
        }
        
        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new BusinessException("Price must be a positive number");
        }
        
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new BusinessException("Quantity must be a positive number");
        }
        
        if (request.getUnit() == null) {
            throw new BusinessException("Unit is required (BOX or PIECE)");
        }
    }
    
    /**
     * Add product to shopping cart
     * User must own a hospital
     * Quantity must be available
     */
    @Transactional
    public ShoppingCartResponse addToCart(AddToCartRequest request, UUID userId) {
        log.info("User {} adding product {} (qty: {}) to cart", userId, request.getProductId(), request.getQuantity());
        
        // Validate request
        if (request.getProductId() == null) {
            throw new BusinessException("Product ID is required");
        }
        
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("Quantity must be greater than 0");
        }
        
        // Find the product
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Product not found with ID: " + request.getProductId()));
        
        // Validate quantity is available
        if (request.getQuantity() > product.getQuantity()) {
            throw new BusinessException(
                "Requested quantity (" + request.getQuantity() + 
                ") exceeds available quantity (" + product.getQuantity() + ")");
        }
        
        // Find user's hospital
        HospitalProfile hospital = hospitalProfileRepository.findByOwnerId(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No hospital profile found for user ID: " + userId));
        
        // Add product to cart via ShopService
        return shopService.addProductToCart(hospital.getId(), product, request.getQuantity(), userId);
    }
}

