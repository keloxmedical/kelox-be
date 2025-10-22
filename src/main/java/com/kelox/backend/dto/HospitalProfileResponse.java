package com.kelox.backend.dto;

import com.kelox.backend.entity.HospitalProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalProfileResponse {
    
    private Long id;
    private String name;
    private String address;
    private String companyName;
    private UUID ownerId;
    private String ownerEmail;
    private String ownerSolanaWallet;
    private List<ContactDto> contacts;
    
    public static HospitalProfileResponse fromEntity(HospitalProfile hospital) {
        HospitalProfileResponse response = new HospitalProfileResponse();
        response.setId(hospital.getId());
        response.setName(hospital.getName());
        response.setAddress(hospital.getAddress());
        response.setCompanyName(hospital.getCompanyName());
        
        if (hospital.getOwner() != null) {
            response.setOwnerId(hospital.getOwner().getId());
            response.setOwnerEmail(hospital.getOwner().getEmail());
            response.setOwnerSolanaWallet(hospital.getOwner().getSolanaWallet());
        }
        
        if (hospital.getContacts() != null) {
            response.setContacts(
                hospital.getContacts().stream()
                    .map(contact -> new ContactDto(
                        contact.getName(),
                        contact.getPosition(),
                        contact.getEmail(),
                        contact.getPhone()
                    ))
                    .collect(Collectors.toList())
            );
        }
        
        return response;
    }
}

