package com.kelox.backend.controller;

import com.kelox.backend.dto.*;
import com.kelox.backend.service.HospitalService;
import com.kelox.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hospitals")
@RequiredArgsConstructor
@Slf4j
public class AdminHospitalController {
    
    private final HospitalService hospitalService;
    private final ProductService productService;
    
    /**
     * Create a new hospital profile
     * Requires: X-Admin-Secret header
     */
    @PostMapping
    public ResponseEntity<HospitalProfileResponse> createHospital(
            @RequestBody CreateHospitalRequest request) {
        
        log.info("Admin API: Creating new hospital profile");
        HospitalProfileResponse response = hospitalService.createHospital(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Assign an owner to a hospital profile
     * Requires: X-Admin-Secret header
     */
    @PutMapping("/{hospitalId}/assign-owner")
    public ResponseEntity<HospitalProfileResponse> assignOwner(
            @PathVariable Long hospitalId,
            @RequestBody AssignOwnerRequest request) {
        
        log.info("Admin API: Assigning owner to hospital {}", hospitalId);
        HospitalProfileResponse response = hospitalService.assignOwner(hospitalId, request.getUserId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all hospitals
     * Requires: X-Admin-Secret header
     */
    @GetMapping
    public ResponseEntity<List<HospitalProfileResponse>> getAllHospitals() {
        log.info("Admin API: Fetching all hospitals");
        List<HospitalProfileResponse> hospitals = hospitalService.getAllHospitals();
        return ResponseEntity.ok(hospitals);
    }
    
    /**
     * Get hospital by ID
     * Requires: X-Admin-Secret header
     */
    @GetMapping("/{hospitalId}")
    public ResponseEntity<HospitalProfileResponse> getHospitalById(@PathVariable Long hospitalId) {
        log.info("Admin API: Fetching hospital {}", hospitalId);
        HospitalProfileResponse response = hospitalService.getHospitalById(hospitalId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Add products for a hospital
     * Requires: X-Admin-Secret header
     */
    @PostMapping("/{hospitalId}/products")
    public ResponseEntity<List<ProductResponse>> addProductsForHospital(
            @PathVariable Long hospitalId,
            @RequestBody AddProductsRequest request) {
        
        log.info("Admin API: Adding {} products for hospital {}", request.getProducts().size(), hospitalId);
        List<ProductResponse> response = productService.addProductsForHospital(hospitalId, request.getProducts());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all products for a hospital
     * Requires: X-Admin-Secret header
     */
    @GetMapping("/{hospitalId}/products")
    public ResponseEntity<List<ProductResponse>> getProductsByHospital(@PathVariable Long hospitalId) {
        log.info("Admin API: Fetching products for hospital {}", hospitalId);
        List<ProductResponse> products = productService.getProductsByHospital(hospitalId);
        return ResponseEntity.ok(products);
    }
}

