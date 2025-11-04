package com.kelox.backend.repository;

import com.kelox.backend.entity.OfferProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfferProductRepository extends JpaRepository<OfferProduct, Long> {
    
    List<OfferProduct> findByOfferId(UUID offerId);
    
    List<OfferProduct> findByProductId(Long productId);
}

