package com.kelox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOffersResponse {
    
    private List<OfferResponse> createdOffers;     // Offers created by the user
    private List<OfferResponse> receivedOffers;    // Offers received by user's hospital
}

