package com.kelox.backend.service;

import com.kelox.backend.dto.OfferMessageResponse;
import com.kelox.backend.entity.HospitalProfile;
import com.kelox.backend.entity.Offer;
import com.kelox.backend.entity.OfferMessage;
import com.kelox.backend.exception.BusinessException;
import com.kelox.backend.exception.ResourceNotFoundException;
import com.kelox.backend.repository.HospitalProfileRepository;
import com.kelox.backend.repository.OfferMessageRepository;
import com.kelox.backend.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatOfferService {
    
    private final OfferRepository offerRepository;
    private final OfferMessageRepository offerMessageRepository;
    private final HospitalProfileRepository hospitalProfileRepository;
    
    /**
     * Send a message in an offer chat
     * User must be either the creator or the hospital owner (seller)
     * Type can be null (normal message) or REJECT
     */
    @Transactional
    public OfferMessageResponse sendMessage(UUID offerId, String message, com.kelox.backend.enums.OfferMessageType type, UUID userId) {
        log.info("User {} sending message to offer {} (type: {})", userId, offerId, type);
        
        // Validate message
        if (message == null || message.trim().isEmpty()) {
            throw new BusinessException("Message cannot be empty");
        }
        
        if (message.length() > 255) {
            throw new BusinessException("Message cannot exceed 255 characters");
        }
        
        // Find the offer
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Offer not found with ID: " + offerId));
        
        // Find user's hospital
        HospitalProfile userHospital = hospitalProfileRepository.findByOwnerId(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No hospital profile found for user ID: " + userId));
        
        // Verify user is either the creator or the seller
        boolean isCreator = offer.getCreator().getId().equals(userId);
        boolean isSeller = offer.getHospital().getOwner() != null && 
                          offer.getHospital().getOwner().getId().equals(userId);
        
        if (!isCreator && !isSeller) {
            throw new BusinessException(
                "User is not authorized to send messages in this offer. " +
                "Only the creator or seller can participate in the chat.");
        }
        
        // Create message
        OfferMessage offerMessage = new OfferMessage();
        offerMessage.setOffer(offer);
        offerMessage.setMessage(message.trim());
        offerMessage.setSenderHospitalName(userHospital.getName());
        offerMessage.setType(type);  // Can be null for normal messages
        
        OfferMessage savedMessage = offerMessageRepository.save(offerMessage);
        log.info("Message sent to offer {} by hospital {} (type: {})", 
            offerId, userHospital.getName(), type != null ? type : "NORMAL");
        
        return OfferMessageResponse.fromEntity(savedMessage);
    }
    
    /**
     * Get all messages for an offer
     * User must be either the creator or the hospital owner (seller)
     */
    @Transactional(readOnly = true)
    public List<OfferMessageResponse> getMessages(UUID offerId, UUID userId) {
        log.info("User {} fetching messages for offer {}", userId, offerId);
        
        // Find the offer
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Offer not found with ID: " + offerId));
        
        // Verify user is either the creator or the seller
        boolean isCreator = offer.getCreator().getId().equals(userId);
        boolean isSeller = offer.getHospital().getOwner() != null && 
                          offer.getHospital().getOwner().getId().equals(userId);
        
        if (!isCreator && !isSeller) {
            throw new BusinessException(
                "User is not authorized to view messages in this offer. " +
                "Only the creator or seller can view the chat.");
        }
        
        // Get messages ordered by creation time
        List<OfferMessage> messages = offerMessageRepository.findByOfferIdOrderByCreatedAtAsc(offerId);
        log.info("Found {} messages for offer {}", messages.size(), offerId);
        
        return messages.stream()
            .map(OfferMessageResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Create a system message for an offer
     * Internal use - automatically generated messages
     * Uses the creator's hospital name as sender
     */
    @Transactional
    public void createSystemMessage(UUID offerId, String message) {
        log.info("Creating system message for offer {}: {}", offerId, message);
        
        // Find the offer
        Offer offer = offerRepository.findById(offerId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Offer not found with ID: " + offerId));
        
        // Get creator's hospital name
        String senderHospitalName = "System";  // Default fallback
        if (offer.getCreator() != null && offer.getCreator().getHospitalProfile() != null) {
            senderHospitalName = offer.getCreator().getHospitalProfile().getName();
        }
        
        // Create system message
        OfferMessage systemMessage = new OfferMessage();
        systemMessage.setOffer(offer);
        systemMessage.setMessage(message);
        systemMessage.setSenderHospitalName(senderHospitalName);
        systemMessage.setType(com.kelox.backend.enums.OfferMessageType.SYSTEM);
        
        offerMessageRepository.save(systemMessage);
        log.info("System message created for offer {} by {}", offerId, senderHospitalName);
    }
}

