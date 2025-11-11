package com.kelox.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hospital_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private Float balance = 0f;

    @OneToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = true, unique = true)
    private User owner;

    @OneToMany(mappedBy = "hospitalProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();
    
    @OneToOne(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private ShoppingCart shoppingCart;
    
    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryAddress> deliveryAddresses = new ArrayList<>();

    // Helper method to add contact
    public void addContact(Contact contact) {
        contacts.add(contact);
        contact.setHospitalProfile(this);
    }

    // Helper method to remove contact
    public void removeContact(Contact contact) {
        contacts.remove(contact);
        contact.setHospitalProfile(null);
    }
    
    // Helper method to add delivery address
    public void addDeliveryAddress(DeliveryAddress address) {
        deliveryAddresses.add(address);
        address.setHospital(this);
    }
    
    // Helper method to remove delivery address
    public void removeDeliveryAddress(DeliveryAddress address) {
        deliveryAddresses.remove(address);
        address.setHospital(null);
    }
}

