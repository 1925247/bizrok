package main.java.com.bizrok.util;

import main.java.com.bizrok.model.entity.Group;
import main.java.com.bizrok.model.entity.Option;
import main.java.com.bizrok.model.entity.OrderAnswer;
import main.java.com.bizrok.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Price Calculator Utility
 * Implements the dynamic pricing logic based on config-driven system
 */
@Component
public class PriceCalculator {
    
    @Autowired
    private SettingsService settingsService;
    
    /**
     * Calculate final price based on model base price and user answers
     */
    public PriceCalculationResult calculatePrice(Double basePrice, List<OrderAnswer> answers) {
        if (basePrice == null || basePrice <= 0) {
            return PriceCalculationResult.error("Invalid base price");
        }
        
        // Get configuration values
        Double minPricePercent = settingsService.getMinPricePercent();
        Double maxDeductionPercent = settingsService.getMaxDeductionPercent();
        
        // Group answers by group
        Map<Long, List<OrderAnswer>> answersByGroup = answers.stream()
                .filter(answer -> answer.getOption() != null)
                .collect(Collectors.groupingBy(
                    answer -> answer.getQuestion().getGroup().getId()
                ));
        
        double totalDeductions = 0.0;
        double groupDeductionsTotal = 0.0;
        
        // Calculate deductions per group
        for (Map.Entry<Long, List<OrderAnswer>> entry : answersByGroup.entrySet()) {
            Long groupId = entry.getKey();
            List<OrderAnswer> groupAnswers = entry.getValue();
            
            double groupDeduction = calculateGroupDeduction(groupId, groupAnswers);
            groupDeductionsTotal += groupDeduction;
        }
        
        // Apply maximum deduction percentage limit
        double maxAllowedDeduction = (basePrice * maxDeductionPercent) / 100.0;
        double finalDeductions = Math.min(groupDeductionsTotal, maxAllowedDeduction);
        
        // Calculate final price
        double finalPrice = basePrice - finalDeductions;
        
        // Apply minimum price percentage
        double minAllowedPrice = (basePrice * minPricePercent) / 100.0;
        finalPrice = Math.max(finalPrice, minAllowedPrice);
        
        return PriceCalculationResult.success(finalPrice, finalDeductions, groupDeductionsTotal);
    }
    
    /**
     * Calculate deduction for a specific group
     */
    private double calculateGroupDeduction(Long groupId, List<OrderAnswer> groupAnswers) {
        // For now, we'll implement a simple logic
        // In a real implementation, you would fetch the Group entity to get the logic type
        
        double groupDeduction = 0.0;
        
        // If group uses MAX logic (take highest deduction in group)
        // If group uses SUM logic (add all deductions in group)
        
        // For this MVP, we'll assume MAX logic for display/body groups
        // and SUM logic for battery/functionality groups
        
        if (isMaxLogicGroup(groupId)) {
            // Take maximum deduction in the group
            groupDeduction = groupAnswers.stream()
                    .mapToDouble(this::getOptionDeduction)
                    .max()
                    .orElse(0.0);
        } else {
            // Sum all deductions in the group
            groupDeduction = groupAnswers.stream()
                    .mapToDouble(this::getOptionDeduction)
                    .sum();
        }
        
        return groupDeduction;
    }
    
    /**
     * Check if group uses MAX logic
     * In a real implementation, this would fetch from database
     */
    private boolean isMaxLogicGroup(Long groupId) {
        // Group IDs 1 (Display) and 2 (Body) use MAX logic
        // Group IDs 3 (Battery) and 4 (Functionality) use SUM logic
        return groupId == 1L || groupId == 2L;
    }
    
    /**
     * Calculate deduction for a specific option
     */
    private double getOptionDeduction(OrderAnswer answer) {
        Option option = answer.getOption();
        if (option == null) {
            return 0.0;
        }
        
        double deductionValue = option.getDeductionValue() != null ? option.getDeductionValue() : 0.0;
        
        if (deductionValue <= 0) {
            return 0.0;
        }
        
        // Handle percentage deductions
        if (option.getDeductionType() == Option.DeductionType.PERCENT) {
            // For percentage deductions, we need the base price
            // This would be passed down or calculated differently
            return deductionValue; // For now, treat as flat amount
        }
        
        return deductionValue;
    }
    
    /**
     * Calculate price without answers (just base price with minimum constraints)
     */
    public PriceCalculationResult calculateBasePrice(Double basePrice) {
        if (basePrice == null || basePrice <= 0) {
            return PriceCalculationResult.error("Invalid base price");
        }
        
        Double minPricePercent = settingsService.getMinPricePercent();
        double minAllowedPrice = (basePrice * minPricePercent) / 100.0;
        
        return PriceCalculationResult.success(minAllowedPrice, 0.0, 0.0);
    }
    
    /**
     * Price Calculation Result
     */
    public static class PriceCalculationResult {
        private final boolean success;
        private final double finalPrice;
        private final double totalDeductions;
        private final double groupDeductionsTotal;
        private final String errorMessage;
        
        private PriceCalculationResult(boolean success, double finalPrice, 
                                     double totalDeductions, double groupDeductionsTotal, 
                                     String errorMessage) {
            this.success = success;
            this.finalPrice = finalPrice;
            this.totalDeductions = totalDeductions;
            this.groupDeductionsTotal = groupDeductionsTotal;
            this.errorMessage = errorMessage;
        }
        
        public static PriceCalculationResult success(double finalPrice, double totalDeductions, double groupDeductionsTotal) {
            return new PriceCalculationResult(true, finalPrice, totalDeductions, groupDeductionsTotal, null);
        }
        
        public static PriceCalculationResult error(String errorMessage) {
            return new PriceCalculationResult(false, 0.0, 0.0, 0.0, errorMessage);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public double getFinalPrice() { return finalPrice; }
        public double getTotalDeductions() { return totalDeductions; }
        public double getGroupDeductionsTotal() { return groupDeductionsTotal; }
        public String getErrorMessage() { return errorMessage; }
    }
}