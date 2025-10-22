package com.kelox.backend.service;

import com.kelox.backend.dto.CreateUserRequest;
import com.kelox.backend.dto.UserResponse;
import com.kelox.backend.entity.User;
import com.kelox.backend.exception.BusinessException;
import com.kelox.backend.exception.ResourceNotFoundException;
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
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with Privy ID: {}, Email: {}", request.getPrivyId(), request.getEmail());
        
        // Validate required fields
        if (request.getPrivyId() == null || request.getPrivyId().trim().isEmpty()) {
            throw new BusinessException("Privy ID is required");
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BusinessException("Email is required");
        }
        
        if (request.getSolanaWallet() == null || request.getSolanaWallet().trim().isEmpty()) {
            throw new BusinessException("Solana wallet is required");
        }
        
        // Check if user already exists
        if (userRepository.existsByPrivyId(request.getPrivyId())) {
            throw new BusinessException("User with Privy ID " + request.getPrivyId() + " already exists");
        }
        
        if (userRepository.existsBySolanaWallet(request.getSolanaWallet())) {
            throw new BusinessException("User with Solana wallet " + request.getSolanaWallet() + " already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("User with email " + request.getEmail() + " already exists");
        }
        
        User user = new User();
        user.setPrivyId(request.getPrivyId());
        user.setEmail(request.getEmail());
        user.setSolanaWallet(request.getSolanaWallet());
        user.setEthereumWallet(request.getEthereumWallet());
        
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());
        
        return UserResponse.fromEntity(savedUser);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        return UserResponse.fromEntity(user);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserBySolanaWallet(String solanaWallet) {
        User user = userRepository.findBySolanaWallet(solanaWallet)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with Solana wallet: " + solanaWallet));
        
        return UserResponse.fromEntity(user);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserByPrivyId(String privyId) {
        User user = userRepository.findByPrivyId(privyId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with Privy ID: " + privyId));
        
        return UserResponse.fromEntity(user);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        return UserResponse.fromEntity(user);
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserResponse::fromEntity)
            .collect(Collectors.toList());
    }
}

