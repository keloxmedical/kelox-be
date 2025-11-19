package com.kelox.backend.repository;

import com.kelox.backend.entity.OfferMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfferMessageRepository extends JpaRepository<OfferMessage, Long> {
    
    List<OfferMessage> findByOfferIdOrderByCreatedAtAsc(UUID offerId);
    
    int countByOfferId(UUID offerId);
}

