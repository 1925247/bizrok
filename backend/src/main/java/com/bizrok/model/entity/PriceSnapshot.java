package com.bizrok.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "price_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceSnapshot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "base_price", nullable = false)
    private Double basePrice;
    
    @Column(name = "total_deductions", nullable = false)
    private Double totalDeductions;
    
    @Column(name = "final_price", nullable = false)
    private Double finalPrice;
    
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Many-to-One relationship with Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculatedAt = LocalDateTime.now();
    }
}