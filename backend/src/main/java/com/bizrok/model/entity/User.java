package com.bizrok.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Entity
 * Represents users in the system with different roles:
 * - USER: Customer
 * - ADMIN: Admin panel user
 * - PARTNER: Partner for order assignment
 * - FIELD_EXECUTIVE: Field executive for QC
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_role", columnList = "role")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String name;
    
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    private Boolean isActive = true;
    
    @Column(name = "kyc_verified")
    private Boolean kycVerified = false;
    
    @Column(name = "kyc_name")
    private String kycName;
    
    @Column(name = "kyc_address", columnDefinition = "TEXT")
    private String kycAddress;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = Role.USER;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.kycVerified == null) {
            this.kycVerified = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum Role {
        USER, ADMIN, PARTNER, FIELD_EXECUTIVE
    }
}