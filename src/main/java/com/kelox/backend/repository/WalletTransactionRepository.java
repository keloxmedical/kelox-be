package com.kelox.backend.repository;

import com.kelox.backend.entity.WalletTransaction;
import com.kelox.backend.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    
    List<WalletTransaction> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);
    
    List<WalletTransaction> findByHospitalIdAndTypeOrderByCreatedAtDesc(Long hospitalId, TransactionType type);
    
    List<WalletTransaction> findByOrderId(UUID orderId);
}

