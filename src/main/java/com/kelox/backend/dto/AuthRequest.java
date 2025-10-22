package com.kelox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    
    // Privy user information
    private String privyId;
    private String email;
    private String solanaWallet;
    private String ethereumWallet;
    
    // Solana signature verification
    private String message;
    private String signature;
}

