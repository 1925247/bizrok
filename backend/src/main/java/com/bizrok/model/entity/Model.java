package com.bizrok.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Model Entity
 * Represents specific device models with base pricing
 * Supports variants (64GB, 128GB, etc.) via JSON field
 */
@Entity
@Table(name = "models", indexes = {
    @Index(name = "idx_models_brand", columnList = "brand_id"),
    @Index(name = "idx_models_category", columnList = "category_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Model {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String slug;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(name = "base_price", nullable = false)
    private Double basePrice;
    
    @Column(name = "variant_info", columnDefinition = "TEXT")
    private String variantInfo; // JSON field for variants like {"variants": ["64GB", "128GB", "256GB"]}
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
        if (this.basePrice == null) {
            this.basePrice = 0.0;
        }
    }
    
    /**
     * Get the category name for this model
     */
    @JsonIgnore
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }
    
    /**
     * Get the brand name for this model
     */
    @JsonIgnore
    public String getBrandName() {
        return brand != null ? brand.getName() : null;
    }
}