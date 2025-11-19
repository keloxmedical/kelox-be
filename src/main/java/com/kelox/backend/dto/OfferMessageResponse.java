package com.kelox.backend.dto;

import com.kelox.backend.entity.OfferMessage;
import com.kelox.backend.enums.OfferMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferMessageResponse {
    
    private Long id;
    private UUID offerId;
    private String message;
    private String senderHospitalName;
    private OfferMessageType type;
    private LocalDateTime createdAt;
    
    public static OfferMessageResponse fromEntity(OfferMessage offerMessage) {
        OfferMessageResponse response = new OfferMessageResponse();
        response.setId(offerMessage.getId());
        response.setMessage(offerMessage.getMessage());
        response.setSenderHospitalName(offerMessage.getSenderHospitalName());
        response.setType(offerMessage.getType());
        response.setCreatedAt(offerMessage.getCreatedAt());
        
        if (offerMessage.getOffer() != null) {
            response.setOfferId(offerMessage.getOffer().getId());
        }
        
        return response;
    }
}

