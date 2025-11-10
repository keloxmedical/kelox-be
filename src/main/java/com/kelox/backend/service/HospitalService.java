package com.kelox.backend.service;

import com.kelox.backend.dto.CreateDeliveryAddressRequest;
import com.kelox.backend.dto.CreateHospitalRequest;
import com.kelox.backend.dto.DeliveryAddressDto;
import com.kelox.backend.dto.HospitalProfileResponse;
import com.kelox.backend.dto.UpdateDeliveryAddressRequest;
import com.kelox.backend.entity.Contact;
import com.kelox.backend.entity.DeliveryAddress;
import com.kelox.backend.entity.HospitalProfile;
import com.kelox.backend.entity.ShoppingCart;
import com.kelox.backend.entity.User;
import com.kelox.backend.exception.BusinessException;
import com.kelox.backend.exception.ResourceNotFoundException;
import com.kelox.backend.repository.DeliveryAddressRepository;
import com.kelox.backend.repository.HospitalProfileRepository;
import com.kelox.backend.repository.ShoppingCartRepository;
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
    private final ShoppingCartRepository shoppingCartRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    
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
        
        // Create shopping cart for the hospital
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setHospital(savedHospital);
        shoppingCartRepository.save(shoppingCart);
        log.info("Shopping cart created for hospital ID: {}", savedHospital.getId());
        
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
     * Get hospital by name
     */
    @Transactional(readOnly = true)
    public HospitalProfileResponse getHospitalByName(String name) {
        log.info("Fetching hospital profile by name: {}", name);
        
        HospitalProfile hospital = hospitalProfileRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Hospital profile not found with name: " + name));
        
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
    
    /**
     * Add delivery address to hospital
     * Only hospital owner can add addresses
     * Maximum 5 addresses per hospital
     */
    @Transactional
    public DeliveryAddressDto addDeliveryAddress(Long hospitalId, CreateDeliveryAddressRequest request, UUID userId) {
        log.info("Adding delivery address for hospital {} by user {}", hospitalId, userId);
        
        // Verify hospital exists and user is owner
        HospitalProfile hospital = verifyHospitalOwner(userId, hospitalId);
        
        // Check maximum limit of 5 addresses
        int currentCount = deliveryAddressRepository.countByHospitalId(hospitalId);
        if (currentCount >= 5) {
            throw new BusinessException("Hospital can have maximum 5 delivery addresses");
        }
        
        // Validate request
        validateDeliveryAddressRequest(request);
        
        // If setting as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetDefaultAddress(hospitalId);
        }
        
        // Create delivery address
        DeliveryAddress address = new DeliveryAddress();
        address.setHospital(hospital);
        address.setStreetAddress(request.getStreetAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        
        DeliveryAddress savedAddress = deliveryAddressRepository.save(address);
        log.info("Delivery address created with ID: {}", savedAddress.getId());
        
        return DeliveryAddressDto.fromEntity(savedAddress);
    }
    
    /**
     * Update delivery address
     * Only hospital owner can update addresses
     */
    @Transactional
    public DeliveryAddressDto updateDeliveryAddress(Long hospitalId, Long addressId, 
                                                    UpdateDeliveryAddressRequest request, UUID userId) {
        log.info("Updating delivery address {} for hospital {} by user {}", addressId, hospitalId, userId);
        
        // Verify hospital exists and user is owner
        verifyHospitalOwner(userId, hospitalId);
        
        // Find address
        DeliveryAddress address = deliveryAddressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Delivery address not found with ID: " + addressId));
        
        // Verify address belongs to hospital
        if (!address.getHospital().getId().equals(hospitalId)) {
            throw new BusinessException("Delivery address does not belong to this hospital");
        }
        
        // Validate request
        validateDeliveryAddressRequest(request);
        
        // If setting as default, unset other defaults
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            unsetDefaultAddress(hospitalId);
        }
        
        // Update address
        address.setStreetAddress(request.getStreetAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        
        DeliveryAddress updatedAddress = deliveryAddressRepository.save(address);
        log.info("Delivery address {} updated successfully", addressId);
        
        return DeliveryAddressDto.fromEntity(updatedAddress);
    }
    
    /**
     * Delete delivery address
     * Only hospital owner can delete addresses
     */
    @Transactional
    public void deleteDeliveryAddress(Long hospitalId, Long addressId, UUID userId) {
        log.info("Deleting delivery address {} for hospital {} by user {}", addressId, hospitalId, userId);
        
        // Verify hospital exists and user is owner
        verifyHospitalOwner(userId, hospitalId);
        
        // Find address
        DeliveryAddress address = deliveryAddressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Delivery address not found with ID: " + addressId));
        
        // Verify address belongs to hospital
        if (!address.getHospital().getId().equals(hospitalId)) {
            throw new BusinessException("Delivery address does not belong to this hospital");
        }
        
        deliveryAddressRepository.delete(address);
        log.info("Delivery address {} deleted successfully", addressId);
    }
    
    /**
     * Get all delivery addresses for a hospital
     * Only hospital owner can view addresses
     */
    @Transactional(readOnly = true)
    public List<DeliveryAddressDto> getDeliveryAddresses(Long hospitalId, UUID userId) {
        log.info("Fetching delivery addresses for hospital {} by user {}", hospitalId, userId);
        
        // Verify hospital exists and user is owner
        verifyHospitalOwner(userId, hospitalId);
        
        return deliveryAddressRepository.findByHospitalId(hospitalId).stream()
            .map(DeliveryAddressDto::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Verify user is the hospital owner
     */
    private HospitalProfile verifyHospitalOwner(UUID userId, Long hospitalId) {
        HospitalProfile hospital = hospitalProfileRepository.findById(hospitalId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Hospital profile not found with ID: " + hospitalId));
        
        if (hospital.getOwner() == null || !hospital.getOwner().getId().equals(userId)) {
            throw new BusinessException(
                "User is not authorized to manage this hospital");
        }
        
        return hospital;
    }
    
    /**
     * Unset default flag on all addresses for a hospital
     */
    private void unsetDefaultAddress(Long hospitalId) {
        deliveryAddressRepository.findByHospitalIdAndIsDefaultTrue(hospitalId)
            .ifPresent(address -> {
                address.setIsDefault(false);
                deliveryAddressRepository.save(address);
            });
    }
    
    /**
     * Validate delivery address request
     */
    private void validateDeliveryAddressRequest(Object request) {
        String streetAddress = null;
        String city = null;
        String postalCode = null;
        String country = null;
        
        if (request instanceof CreateDeliveryAddressRequest) {
            CreateDeliveryAddressRequest req = (CreateDeliveryAddressRequest) request;
            streetAddress = req.getStreetAddress();
            city = req.getCity();
            postalCode = req.getPostalCode();
            country = req.getCountry();
        } else if (request instanceof UpdateDeliveryAddressRequest) {
            UpdateDeliveryAddressRequest req = (UpdateDeliveryAddressRequest) request;
            streetAddress = req.getStreetAddress();
            city = req.getCity();
            postalCode = req.getPostalCode();
            country = req.getCountry();
        }
        
        if (streetAddress == null || streetAddress.trim().isEmpty()) {
            throw new BusinessException("Street address is required");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new BusinessException("City is required");
        }
        if (postalCode == null || postalCode.trim().isEmpty()) {
            throw new BusinessException("Postal code is required");
        }
        if (country == null || country.trim().isEmpty()) {
            throw new BusinessException("Country is required");
        }
    }
}

