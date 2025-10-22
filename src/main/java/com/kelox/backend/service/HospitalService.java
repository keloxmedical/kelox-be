package com.kelox.backend.service;

import com.kelox.backend.dto.CreateHospitalRequest;
import com.kelox.backend.dto.HospitalProfileResponse;
import com.kelox.backend.entity.Contact;
import com.kelox.backend.entity.HospitalProfile;
import com.kelox.backend.entity.User;
import com.kelox.backend.exception.BusinessException;
import com.kelox.backend.exception.ResourceNotFoundException;
import com.kelox.backend.repository.HospitalProfileRepository;
import com.kelox.backend.repository.UserRepository;
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
public class HospitalService {
    
    private final HospitalProfileRepository hospitalProfileRepository;
    private final UserRepository userRepository;
    
    /**
     * Create a new hospital profile without an owner
     */
    @Transactional
    public HospitalProfileResponse createHospital(CreateHospitalRequest request) {
        log.info("Creating new hospital profile: {}", request.getName());
        
        // Validate required fields
        validateHospitalRequest(request);
        
        // Create hospital profile
        HospitalProfile hospital = new HospitalProfile();
        hospital.setName(request.getName());
        hospital.setAddress(request.getAddress());
        hospital.setCompanyName(request.getCompanyName());
        
        // Add contacts if provided
        if (request.getContacts() != null && !request.getContacts().isEmpty()) {
            List<Contact> contacts = request.getContacts().stream()
                .map(contactDto -> {
                    Contact contact = new Contact();
                    contact.setName(contactDto.getName());
                    contact.setPosition(contactDto.getPosition());
                    contact.setEmail(contactDto.getEmail());
                    contact.setPhone(contactDto.getPhone());
                    return contact;
                })
                .collect(Collectors.toList());
            
            // Use helper method to maintain bidirectional relationship
            contacts.forEach(hospital::addContact);
        }
        
        HospitalProfile savedHospital = hospitalProfileRepository.save(hospital);
        log.info("Hospital profile created with ID: {}", savedHospital.getId());
        
        return HospitalProfileResponse.fromEntity(savedHospital);
    }
    
    /**
     * Assign an owner to a hospital profile
     * Ensures 1:1 relationship - one user can only own one hospital
     */
    @Transactional
    public HospitalProfileResponse assignOwner(Long hospitalId, UUID userId) {
        log.info("Assigning owner {} to hospital {}", userId, hospitalId);
        
        // Find the hospital
        HospitalProfile hospital = hospitalProfileRepository.findById(hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Hospital profile not found with ID: " + hospitalId));
        
        // Find the user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "User not found with ID: " + userId));
        
        // Check if user is already assigned to another hospital
        if (hospitalProfileRepository.existsByOwnerId(userId)) {
            throw new BusinessException(
                "User with ID " + userId + " is already assigned as owner to another hospital");
        }
        
        // Check if hospital already has an owner
        if (hospital.getOwner() != null) {
            throw new BusinessException(
                "Hospital profile already has an owner: " + hospital.getOwner().getSolanaWallet());
        }
        
        // Assign the owner
        hospital.setOwner(user);
        HospitalProfile updatedHospital = hospitalProfileRepository.save(hospital);
        
        log.info("Owner {} successfully assigned to hospital {}", userId, hospitalId);
        
        return HospitalProfileResponse.fromEntity(updatedHospital);
    }
    
    /**
     * Get hospital by ID
     */
    @Transactional(readOnly = true)
    public HospitalProfileResponse getHospitalById(Long hospitalId) {
        HospitalProfile hospital = hospitalProfileRepository.findById(hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Hospital profile not found with ID: " + hospitalId));
        
        return HospitalProfileResponse.fromEntity(hospital);
    }
    
    /**
     * Get all hospitals
     */
    @Transactional(readOnly = true)
    public List<HospitalProfileResponse> getAllHospitals() {
        return hospitalProfileRepository.findAll().stream()
            .map(HospitalProfileResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get hospital by owner ID
     */
    @Transactional(readOnly = true)
    public HospitalProfileResponse getHospitalByOwnerId(UUID ownerId) {
        HospitalProfile hospital = hospitalProfileRepository.findByOwnerId(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No hospital profile found for owner ID: " + ownerId));
        
        return HospitalProfileResponse.fromEntity(hospital);
    }
    
    /**
     * Validate hospital request
     */
    private void validateHospitalRequest(CreateHospitalRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException("Hospital name is required");
        }
        
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new BusinessException("Hospital address is required");
        }
        
        if (request.getCompanyName() == null || request.getCompanyName().trim().isEmpty()) {
            throw new BusinessException("Company name is required");
        }
        
        // Validate contacts if provided
        if (request.getContacts() != null) {
            request.getContacts().forEach(contact -> {
                if (contact.getName() == null || contact.getName().trim().isEmpty()) {
                    throw new BusinessException("Contact name is required");
                }
                if (contact.getEmail() == null || contact.getEmail().trim().isEmpty()) {
                    throw new BusinessException("Contact email is required");
                }
            });
        }
    }
}

