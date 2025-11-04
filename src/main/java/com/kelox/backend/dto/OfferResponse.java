package com.kelox.backend.dto;

import com.kelox.backend.entity.Offer;
import com.kelox.backend.enums.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferResponse {
    
    private UUID id;
    private Long hospitalId;
    private String hospitalName;
    private UUID creatorId;
    private String creatorHospitalName;
    private List<OfferProductDto> products;
    private LocalDateTime createdAt;
    private OfferStatus status;
    
    public static OfferResponse fromEntity(Offer offer) {
        OfferResponse response = new OfferResponse();
        response.setId(offer.getId());
        response.setCreatedAt(offer.getCreatedAt());
        response.setStatus(offer.getStatus());
        
        if (offer.getHospital() != null) {
            response.setHospitalId(offer.getHospital().getId());
            response.setHospitalName(offer.getHospital().getName());
        }
        
        if (offer.getCreator() != null) {
            response.setCreatorId(offer.getCreator().getId());
            
            // Set creator's hospital name if they own a hospital
            if (offer.getCreator().getHospitalProfile() != null) {
                response.setCreatorHospitalName(offer.getCreator().getHospitalProfile().getName());
            }
        }
        
        if (offer.getOfferProducts() != null && !offer.getOfferProducts().isEmpty()) {
            response.setProducts(offer.getOfferProducts().stream()
                .map(OfferProductDto::fromEntity)
                .collect(Collectors.toList()));
        }
        
        return response;
    }
}

