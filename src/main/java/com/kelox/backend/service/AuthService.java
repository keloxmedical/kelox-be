package com.kelox.backend.service;

import com.kelox.backend.dto.AuthRequest;
import com.kelox.backend.dto.AuthResponse;
import com.kelox.backend.dto.HospitalProfileResponse;
import com.kelox.backend.entity.HospitalProfile;
import com.kelox.backend.entity.User;
import com.kelox.backend.exception.UnauthorizedException;
import com.kelox.backend.repository.HospitalProfileRepository;
import com.kelox.backend.repository.UserRepository;
import com.kelox.backend.util.JwtUtil;
import com.kelox.backend.util.SolanaSignatureVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final HospitalProfileRepository hospitalProfileRepository;
    private final SolanaSignatureVerifier signatureVerifier;
    private final JwtUtil jwtUtil;
    
    /**
     * Authenticate user with Solana wallet signature from Privy
     * - Validates Privy user data
     * - Verifies Solana signature
     * - Creates user if doesn't exist
     * - Updates user info if exists
     * - Generates JWT token
     */
    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authentication attempt for Privy ID: {}, Email: {}", request.getPrivyId(), request.getEmail());
        
        // Validate Privy data
        validatePrivyData(request);
        
        // Verify Solana signature
        verifySolanaSignature(request);
        
        log.info("Signature verified successfully for Solana wallet: {}", request.getSolanaWallet());
        
        // Check if user exists by Privy ID or Solana wallet
        Optional<User> existingUser = userRepository.findByPrivyId(request.getPrivyId());
        
        if (existingUser.isEmpty()) {
            // Try to find by Solana wallet (in case of migration)
            existingUser = userRepository.findBySolanaWallet(request.getSolanaWallet());
        }
        
        User user;
        boolean isNewUser;
        
        if (existingUser.isPresent()) {
            // User exists - update their information
            user = existingUser.get();
            user.setPrivyId(request.getPrivyId());
            user.setEmail(request.getEmail());
            user.setSolanaWallet(request.getSolanaWallet());
            user.setEthereumWallet(request.getEthereumWallet());
            user = userRepository.save(user);
            isNewUser = false;
            log.info("Existing user authenticated and updated: {}", user.getId());
        } else {
            // Create new user
            user = new User();
            user.setPrivyId(request.getPrivyId());
            user.setEmail(request.getEmail());
            user.setSolanaWallet(request.getSolanaWallet());
            user.setEthereumWallet(request.getEthereumWallet());
            user = userRepository.save(user);
            isNewUser = true;
            log.info("New user created: {}", user.getId());
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getSolanaWallet());
        
        // Check if user owns a hospital
        HospitalProfileResponse hospitalProfile = null;
        Optional<HospitalProfile> hospital = hospitalProfileRepository.findByOwnerId(user.getId());
        if (hospital.isPresent()) {
            hospitalProfile = HospitalProfileResponse.fromEntity(hospital.get());
            log.info("User owns hospital: {}", hospitalProfile.getName());
        } else {
            log.info("User does not own a hospital");
        }
        
        return new AuthResponse(
            token,
            user.getId(),
            user.getPrivyId(),
            user.getEmail(),
            user.getSolanaWallet(),
            user.getEthereumWallet(),
            isNewUser,
            hospitalProfile
        );
    }
    
    /**
     * Validate Privy user data
     */
    private void validatePrivyData(AuthRequest request) {
        if (request.getPrivyId() == null || request.getPrivyId().trim().isEmpty()) {
            throw new UnauthorizedException("Privy ID is required");
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new UnauthorizedException("Email is required");
        }
        
        if (request.getSolanaWallet() == null || request.getSolanaWallet().trim().isEmpty()) {
            throw new UnauthorizedException("Solana wallet is required");
        }
        
        // Validate Solana wallet format
        if (!signatureVerifier.isValidWalletAddress(request.getSolanaWallet())) {
            throw new UnauthorizedException("Invalid Solana wallet address");
        }
    }
    
    /**
     * Verify Solana signature
     */
    private void verifySolanaSignature(AuthRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new UnauthorizedException("Message is required for signature verification");
        }
        
        if (request.getSignature() == null || request.getSignature().trim().isEmpty()) {
            throw new UnauthorizedException("Signature is required");
        }
        
        // Verify signature using Solana wallet as public key
        boolean isValid = signatureVerifier.verifySignature(
            request.getMessage(),
            request.getSignature(),
            request.getSolanaWallet()
        );
        
        if (!isValid) {
            log.warn("Invalid signature for Solana wallet: {}", request.getSolanaWallet());
            throw new UnauthorizedException("Invalid Solana signature");
        }
    }
    
    /**
     * Validate JWT token and return user ID
     */
    public UUID validateToken(String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token");
        }
        
        return jwtUtil.getUserIdFromToken(token);
    }
    
    /**
     * Refresh token (generate new token for existing user)
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String oldToken) {
        UUID userId = validateToken(oldToken);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        String newToken = jwtUtil.generateToken(user.getId(), user.getSolanaWallet());
        
        // Check if user owns a hospital
        HospitalProfileResponse hospitalProfile = null;
        Optional<HospitalProfile> hospital = hospitalProfileRepository.findByOwnerId(user.getId());
        if (hospital.isPresent()) {
            hospitalProfile = HospitalProfileResponse.fromEntity(hospital.get());
        }
        
        return new AuthResponse(
            newToken,
            user.getId(),
            user.getPrivyId(),
            user.getEmail(),
            user.getSolanaWallet(),
            user.getEthereumWallet(),
            false,
            hospitalProfile
        );
    }
}

