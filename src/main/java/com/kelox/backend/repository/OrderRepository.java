package com.kelox.backend.repository;

import com.kelox.backend.entity.Order;
import com.kelox.backend.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    List<Order> findByHospitalId(Long hospitalId);
    
    List<Order> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByHospitalIdAndStatus(Long hospitalId, OrderStatus status);
    
    List<Order> findByHospitalIdAndStatusOrderByCreatedAtDesc(Long hospitalId, OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.hospital.id = :hospitalId AND o.status NOT IN ('COMPLETED', 'CANCELED') ORDER BY o.createdAt DESC")
    List<Order> findPendingOrdersByHospitalId(@Param("hospitalId") Long hospitalId);
}

