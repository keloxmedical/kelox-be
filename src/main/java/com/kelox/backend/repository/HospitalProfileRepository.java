package com.kelox.backend.repository;

import com.kelox.backend.entity.HospitalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HospitalProfileRepository extends JpaRepository<HospitalProfile, Long> {
    
    Optional<HospitalProfile> findByOwnerId(UUID ownerId);
    
    boolean existsByOwnerId(UUID ownerId);
}

