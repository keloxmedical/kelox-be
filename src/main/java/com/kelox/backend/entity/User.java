package com.kelox.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String privyId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String solanaWallet;

    @Column(nullable = true, unique = true)
    private String ethereumWallet;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private HospitalProfile hospitalProfile;
}

