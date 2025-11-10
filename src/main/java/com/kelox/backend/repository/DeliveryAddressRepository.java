package com.kelox.backend.repository;

import com.kelox.backend.entity.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    
    List<DeliveryAddress> findByHospitalId(Long hospitalId);
    
    int countByHospitalId(Long hospitalId);
    
    Optional<DeliveryAddress> findByHospitalIdAndIsDefaultTrue(Long hospitalId);
}

