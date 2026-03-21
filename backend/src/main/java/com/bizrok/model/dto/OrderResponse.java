package main.java.com.bizrok.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Response DTO
 * Used for returning order details with pricing information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    
    private Long id;
    private String orderNumber;
    private String status;
    private ModelDto model;
    private Double basePrice;
    private Double finalPrice;
    private Double totalDeductions;
    private String pickupAddress;
    private String pickupPincode;
    private LocalDateTime pickupDate;
    private String pickupTime;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankAccountName;
    private Boolean kycVerified;
    private Boolean faceMatchVerified;
    private Boolean bankDetailsVerified;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderAnswerDto> answers;
    private List<PriceSnapshotDto> priceSnapshots;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderAnswerDto {
        private Long id;
        private QuestionDto.QuestionDto question;
        private OptionDto option;
        private String answerText;
        private String imageUrl;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceSnapshotDto {
        private Long id;
        private Double basePrice;
        private String groupDeductions;
        private Double totalDeductions;
        private Double finalPrice;
        private LocalDateTime calculatedAt;
    }
}