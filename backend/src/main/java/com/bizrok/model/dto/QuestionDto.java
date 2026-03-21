package main.java.com.bizrok.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Question DTO
 * Used for frontend question flow with options
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDto {
    
    private Long id;
    private String text;
    private String slug;
    private String groupName;
    private String subGroupName;
    private String questionType;
    private Boolean isRequired;
    private Boolean isActive;
    private Integer sortOrder;
    private List<OptionDto> options;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionDto {
        private Long id;
        private String text;
        private String slug;
        private Double deductionValue;
        private String deductionType;
        private String imageUrl;
        private Boolean isActive;
        private Integer sortOrder;
    }
}