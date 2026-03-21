package main.java.com.bizrok.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Settings Entity
 * Config-driven system settings stored in database
 * Allows admin to control system behavior without code changes
 */
@Entity
@Table(name = "settings", uniqueConstraints = {
    @UniqueConstraint(columnNames = "key")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String key;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;
    
    @Column(name = "data_type")
    private String dataType = "string"; // string, number, boolean
    
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.dataType == null) {
            this.dataType = "string";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get value as boolean
     */
    public Boolean getBooleanValue() {
        return Boolean.parseBoolean(this.value);
    }
    
    /**
     * Get value as integer
     */
    public Integer getIntegerValue() {
        return Integer.parseInt(this.value);
    }
    
    /**
     * Get value as double
     */
    public Double getDoubleValue() {
        return Double.parseDouble(this.value);
    }
}