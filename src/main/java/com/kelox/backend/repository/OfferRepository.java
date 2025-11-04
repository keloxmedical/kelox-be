package com.kelox.backend.repository;

import com.kelox.backend.entity.Offer;
import com.kelox.backend.enums.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {
    
    List<Offer> findByHospitalId(Long hospitalId);
    
    List<Offer> findByCreatorId(UUID creatorId);
    
    List<Offer> findByStatus(OfferStatus status);
    
    List<Offer> findByHospitalIdAndStatus(Long hospitalId, OfferStatus status);
    
    List<Offer> findByCreatorIdAndStatus(UUID creatorId, OfferStatus status);
    
    Optional<Offer> findByCreatorIdAndHospitalIdAndStatus(UUID creatorId, Long hospitalId, OfferStatus status);
    
    boolean existsByCreatorIdAndHospitalIdAndStatus(UUID creatorId, Long hospitalId, OfferStatus status);
    
    List<Offer> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId);
    
    List<Offer> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);
}

