package main.java.com.bizrok.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Pincode Entity
 * Represents serviceable areas and partner assignments
 */
@Entity
@Table(name = "pincodes", uniqueConstraints = {
    @UniqueConstraint(columnNames = "pincode")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pincode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String pincode;
    
    private String city;
    
    private String state;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private User partner; // Assigned partner for this pincode
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}