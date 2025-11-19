package com.kelox.backend.dto;

import com.kelox.backend.enums.OfferMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendOfferMessageRequest {
    
    private String message;
    private OfferMessageType type;  // Optional: null for normal message, REJECTED for rejection
}

