package com.bizrok.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Email OTP Entity
 * Stores OTP codes for email authentication
 * Supports max 3 attempts with 5-minute expiry
 */
@Entity
@Table(name = "email_otp", indexes = {
    @Index(name = "idx_email_otp_email", columnList = "email"),
    @Index(name = "idx_email_otp_expiry", columnList = "expiry_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailOtp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String otp;
    
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;
    
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
    private Integer attempts = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.attempts == null) {
            this.attempts = 0;
        }
        if (this.isVerified == null) {
            this.isVerified = false;
        }
    }
    
    /**
     * Check if OTP is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryTime);
    }
    
    /**
     * Increment attempt count
     */
    public void incrementAttempts() {
        this.attempts++;
    }
    
    /**
     * Mark OTP as verified
     */
    public void markAsVerified() {
        this.isVerified = true;
    }
}