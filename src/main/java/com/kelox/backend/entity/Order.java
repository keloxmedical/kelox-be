package com.kelox.backend.entity;

import com.kelox.backend.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private HospitalProfile hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id", nullable = false)
    private DeliveryAddress deliveryAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Float productsCost;

    @Column(nullable = false)
    private Float platformFee;

    @Column(nullable = true)
    private Float deliveryFee;

    @Column(nullable = false)
    private Float totalCost;

    @Column(nullable = false)
    private Boolean paid = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = OrderStatus.CALCULATING_LOGISTICS;
        }
        // Calculate total cost
        calculateTotalCost();
    }

    @PreUpdate
    protected void onUpdate() {
        // Set completedAt when status changes to COMPLETED
        if (status == OrderStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
        // Recalculate total cost
        calculateTotalCost();
    }

    // Helper method to calculate total cost
    private void calculateTotalCost() {
        Float products = productsCost != null ? productsCost : 0f;
        Float platform = platformFee != null ? platformFee : 0f;
        Float delivery = deliveryFee != null ? deliveryFee : 0f;  // 0 if null
        totalCost = products + platform + delivery;
    }

    // Helper method to add order item
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    // Helper method to remove order item
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }
}

