package com.kelox.backend.dto;

import com.kelox.backend.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {
    
    private TransactionType type;
    private Float amount;
    private String description;
    private UUID orderId;  // Optional: link transaction to an order
}

