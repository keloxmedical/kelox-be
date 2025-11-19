package com.kelox.backend.entity;

import com.kelox.backend.enums.OfferMessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "offer_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(nullable = false)
    private String senderHospitalName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private OfferMessageType type;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

