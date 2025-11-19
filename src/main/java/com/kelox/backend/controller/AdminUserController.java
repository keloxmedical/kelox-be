package com.kelox.backend.controller;

import com.kelox.backend.dto.CreateUserRequest;
import com.kelox.backend.dto.UserResponse;
import com.kelox.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {
    
    private final UserService userService;
    
    /**
     * Create a new user
     * Requires: X-Admin-Secret header
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        log.info("Admin API: Creating new user");
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all users
     * Requires: X-Admin-Secret header
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Admin API: Fetching all users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by ID
     * Requires: X-Admin-Secret header
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.info("Admin API: Fetching user {}", userId);
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user by Solana wallet
     * Requires: X-Admin-Secret header
     */
    @GetMapping("/solana-wallet/{solanaWallet}")
    public ResponseEntity<UserResponse> getUserBySolanaWallet(@PathVariable String solanaWallet) {
        log.info("Admin API: Fetching user by Solana wallet {}", solanaWallet);
        UserResponse response = userService.getUserBySolanaWallet(solanaWallet);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user by Privy ID
     * Requires: X-Admin-Secret header
     */
    @GetMapping("/privy/{privyId}")
    public ResponseEntity<UserResponse> getUserByPrivyId(@PathVariable String privyId) {
        log.info("Admin API: Fetching user by Privy ID {}", privyId);
        UserResponse response = userService.getUserByPrivyId(privyId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user by email
     * Requires: X-Admin-Secret header
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("Admin API: Fetching user by email {}", email);
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }
}

