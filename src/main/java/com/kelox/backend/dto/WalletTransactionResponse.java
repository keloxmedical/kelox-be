package com.kelox.backend.dto;

import com.kelox.backend.entity.WalletTransaction;
import com.kelox.backend.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionResponse {
    
    private Long id;
    private Long hospitalId;
    private String hospitalName;
    private TransactionType type;
    private Float amount;
    private String description;
    private UUID orderId;
    private Float balanceBefore;
    private Float balanceAfter;
    private LocalDateTime createdAt;
    
    public static WalletTransactionResponse fromEntity(WalletTransaction transaction) {
        WalletTransactionResponse response = new WalletTransactionResponse();
        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setBalanceBefore(transaction.getBalanceBefore());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setCreatedAt(transaction.getCreatedAt());
        
        if (transaction.getHospital() != null) {
            response.setHospitalId(transaction.getHospital().getId());
            response.setHospitalName(transaction.getHospital().getName());
        }
        
        if (transaction.getOrder() != null) {
            response.setOrderId(transaction.getOrder().getId());
        }
        
        return response;
    }
}

