package com.kelox.backend.controller;

import com.kelox.backend.dto.ProductResponse;
import com.kelox.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
@Slf4j
public class MarketplaceController {
    
    private final ProductService productService;
    
    /**
     * Get all products in the marketplace
     * Public endpoint - no authentication required
     * 
     * @return List of all available products
     */
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Fetching all products from marketplace");
        List<ProductResponse> products = productService.getAllProducts();
        log.info("Found {} products in marketplace", products.size());
        return ResponseEntity.ok(products);
    }
    
    /**
     * Get a specific product by ID
     * Public endpoint - no authentication required
     * 
     * @param productId The ID of the product to retrieve
     * @return Product details
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        log.info("Fetching product with ID: {} from marketplace", productId);
        ProductResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }
    
    /**
     * Get all products from a specific hospital
     * Public endpoint - no authentication required
     * 
     * @param hospitalId The ID of the hospital
     * @return List of products from the specified hospital
     */
    @GetMapping("/hospitals/{hospitalId}/products")
    public ResponseEntity<List<ProductResponse>> getProductsByHospital(@PathVariable Long hospitalId) {
        log.info("Fetching products for hospital ID: {} from marketplace", hospitalId);
        List<ProductResponse> products = productService.getProductsByHospital(hospitalId);
        log.info("Found {} products for hospital ID: {}", products.size(), hospitalId);
        return ResponseEntity.ok(products);
    }
}

