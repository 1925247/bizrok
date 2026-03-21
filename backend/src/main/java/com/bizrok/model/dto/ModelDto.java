package main.java.com.bizrok.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model DTO
 * Used for frontend device selection flow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelDto {
    
    private Long id;
    private String name;
    private String slug;
    private String brandName;
    private String categoryName;
    private Double basePrice;
    private String variantInfo;
    private String imageUrl;
    private Boolean isActive;
    private Integer sortOrder;
}