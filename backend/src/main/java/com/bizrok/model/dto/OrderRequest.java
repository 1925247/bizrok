package main.java.com.bizrok.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Request DTO
 * Used for creating new orders with device selection and answers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    
    @NotNull(message = "Model ID is required")
    private Long modelId;
    
    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;
    
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^\\d{6}$", message = "Invalid pincode format")
    private String pickupPincode;
    
    @NotNull(message = "Pickup date is required")
    private LocalDateTime pickupDate;
    
    @NotBlank(message = "Pickup time is required")
    private String pickupTime;
    
    @NotBlank(message = "Bank account number is required")
    @Pattern(regexp = "^\\d{9,18}$", message = "Invalid bank account number")
    private String bankAccountNumber;
    
    @NotBlank(message = "Bank IFSC is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
    private String bankIfsc;
    
    @NotBlank(message = "Bank account name is required")
    private String bankAccountName;
    
    private List<AnswerRequest> answers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerRequest {
        private Long questionId;
        private Long optionId;
        private String answerText;
        private String imageUrl;
    }
}