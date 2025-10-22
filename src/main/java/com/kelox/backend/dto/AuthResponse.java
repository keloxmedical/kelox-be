package com.kelox.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private UUID userId;
    private String privyId;
    private String email;
    private String solanaWallet;
    private String ethereumWallet;
    private boolean newUser;
    private HospitalProfileResponse hospitalProfile;
}

