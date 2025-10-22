package com.kelox.backend.repository;

import com.kelox.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findBySellerIdOrderByExpiryDateAsc(Long sellerId);
    
    List<Product> findByCodeAndLotNumber(String code, String lotNumber);
    
    boolean existsByCodeAndLotNumberAndSellerId(String code, String lotNumber, Long sellerId);
}

