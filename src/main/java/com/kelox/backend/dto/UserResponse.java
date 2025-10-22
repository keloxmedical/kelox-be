package com.kelox.backend.dto;

import com.kelox.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private UUID id;
    private String privyId;
    private String email;
    private String solanaWallet;
    private String ethereumWallet;
    
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getPrivyId(),
            user.getEmail(),
            user.getSolanaWallet(),
            user.getEthereumWallet()
        );
    }
}

