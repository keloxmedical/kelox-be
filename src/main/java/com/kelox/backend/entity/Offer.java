package com.kelox.backend.entity;

import com.kelox.backend.enums.OfferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private HospitalProfile hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferProduct> offerProducts = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = OfferStatus.PENDING;
        }
    }

    // Helper method to add offer product
    public void addOfferProduct(OfferProduct offerProduct) {
        offerProducts.add(offerProduct);
        offerProduct.setOffer(this);
    }

    // Helper method to remove offer product
    public void removeOfferProduct(OfferProduct offerProduct) {
        offerProducts.remove(offerProduct);
        offerProduct.setOffer(null);
    }
}

