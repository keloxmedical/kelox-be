package com.kelox.backend.repository;

import com.kelox.backend.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {
    
    Optional<ShoppingCart> findByHospitalId(Long hospitalId);
    
    boolean existsByHospitalId(Long hospitalId);
}

