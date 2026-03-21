package main.java.com.bizrok.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Entity
 * Represents device buyback orders with complete lifecycle management
 * Status: CREATED → ASSIGNED → IN_PROGRESS → QC_DONE → COMPLETED → REJECTED
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user", columnList = "user_id"),
    @Index(name = "idx_orders_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.CREATED;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo; // Partner or Field Executive
    
    @Column(name = "pickup_address", columnDefinition = "TEXT")
    private String pickupAddress;
    
    @Column(name = "pickup_pincode")
    private String pickupPincode;
    
    @Column(name = "pickup_date")
    private LocalDateTime pickupDate;
    
    @Column(name = "pickup_time")
    private String pickupTime;
    
    @Column(name = "bank_account_number")
    private String bankAccountNumber;
    
    @Column(name = "bank_ifsc")
    private String bankIfsc;
    
    @Column(name = "bank_account_name")
    private String bankAccountName;
    
    @Column(name = "final_price")
    private Double finalPrice;
    
    @Column(name = "base_price")
    private Double basePrice;
    
    @Column(name = "total_deductions")
    private Double totalDeductions;
    
    @Column(name = "kyc_verified")
    private Boolean kycVerified = false;
    
    @Column(name = "face_match_verified")
    private Boolean faceMatchVerified = false;
    
    @Column(name = "bank_details_verified")
    private Boolean bankDetailsVerified = false;
    
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderAnswer> answers;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PriceSnapshot> priceSnapshots;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = Status.CREATED;
        }
        if (this.kycVerified == null) {
            this.kycVerified = false;
        }
        if (this.faceMatchVerified == null) {
            this.faceMatchVerified = false;
        }
        if (this.bankDetailsVerified == null) {
            this.bankDetailsVerified = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum Status {
        CREATED, ASSIGNED, IN_PROGRESS, QC_DONE, COMPLETED, REJECTED
    }
}