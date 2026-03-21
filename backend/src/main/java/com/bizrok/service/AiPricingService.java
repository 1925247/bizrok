package com.bizrok.service;

import com.bizrok.model.entity.Model;
import com.bizrok.model.entity.Order;
import com.bizrok.model.entity.PriceSnapshot;
import com.bizrok.model.entity.OrderAnswer;
import com.bizrok.repository.ModelRepository;
import com.bizrok.repository.OrderRepository;
import com.bizrok.repository.PriceSnapshotRepository;
import com.bizrok.util.PriceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiPricingService {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PriceSnapshotRepository priceSnapshotRepository;

    @Autowired
    private PriceCalculator priceCalculator;

    /**
     * AI-powered dynamic pricing based on market trends and historical data
     */
    public Map<String, Object> calculateAiPricing(Long modelId, List<OrderAnswer> answers) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Model not found"));

        // Base price from model
        double basePrice = model.getBasePrice();
        
        // Calculate condition-based deductions
        double conditionDeductions = calculateConditionDeductions(answers, model);
        
        // Get market trend adjustments
        double marketAdjustment = calculateMarketTrendAdjustment(model);
        
        // Get seasonal adjustments
        double seasonalAdjustment = calculateSeasonalAdjustment(model);
        
        // Get competitor price adjustments
        double competitorAdjustment = calculateCompetitorAdjustment(model);
        
        // Calculate final AI-powered price
        double finalPrice = calculateFinalAiPrice(
            basePrice, 
            conditionDeductions, 
            marketAdjustment, 
            seasonalAdjustment, 
            competitorAdjustment
        );

        // Generate pricing breakdown
        Map<String, Object> pricingBreakdown = generatePricingBreakdown(
            basePrice, 
            conditionDeductions, 
            marketAdjustment, 
            seasonalAdjustment, 
            competitorAdjustment, 
            finalPrice
        );

        return pricingBreakdown;
    }

    /**
     * Calculate condition-based deductions using AI analysis
     */
    private double calculateConditionDeductions(List<OrderAnswer> answers, Model model) {
        double totalDeductions = 0.0;

        // Analyze each answer for condition impact
        for (OrderAnswer answer : answers) {
            if (answer.getQuestion() != null && answer.getQuestion().getQuestionType() == 1) { // Radio type
                // Apply AI-based deduction logic
                double deduction = calculateAiDeduction(answer, model);
                totalDeductions += deduction;
            }
        }

        // Apply machine learning model for complex condition analysis
        double mlDeduction = applyMachineLearningDeduction(answers, model);
        totalDeductions += mlDeduction;

        return totalDeductions;
    }

    /**
     * AI-based deduction calculation
     */
    private double calculateAiDeduction(OrderAnswer answer, Model model) {
        // Simple rule-based AI for now (can be enhanced with ML models)
        double baseDeduction = 0.0;
        
        if (answer.getOption() != null) {
            // Weighted deduction based on option severity
            baseDeduction = answer.getOption().getDeductionValue() * 
                           getConditionWeight(answer.getQuestion().getId());
        }

        // Apply model-specific adjustments
        baseDeduction *= getModelConditionMultiplier(model);

        return baseDeduction;
    }

    /**
     * Machine learning-based deduction calculation
     */
    private double applyMachineLearningDeduction(List<OrderAnswer> answers, Model model) {
        // Placeholder for ML model integration
        // In production, this would use TensorFlow, PyTorch, or similar
        
        double mlScore = 0.0;
        
        // Analyze answer patterns
        Map<String, Integer> answerPatterns = analyzeAnswerPatterns(answers);
        
        // Apply pattern-based deductions
        for (Map.Entry<String, Integer> pattern : answerPatterns.entrySet()) {
            mlScore += calculatePatternScore(pattern.getKey(), pattern.getValue(), model);
        }

        return mlScore;
    }

    /**
     * Analyze answer patterns for ML processing
     */
    private Map<String, Integer> analyzeAnswerPatterns(List<OrderAnswer> answers) {
        Map<String, Integer> patterns = new HashMap<>();
        
        for (OrderAnswer answer : answers) {
            if (answer.getQuestion() != null) {
                String patternKey = "Q" + answer.getQuestion().getId() + "_A" + 
                                  (answer.getOption() != null ? answer.getOption().getId() : "NULL");
                patterns.put(patternKey, patterns.getOrDefault(patternKey, 0) + 1);
            }
        }

        return patterns;
    }

    /**
     * Calculate pattern-based ML score
     */
    private double calculatePatternScore(String pattern, Integer count, Model model) {
        // Simple scoring algorithm (can be replaced with ML model)
        double baseScore = 0.0;
        
        if (pattern.contains("Q1")) { // Screen condition
            baseScore = count * 50.0;
        } else if (pattern.contains("Q2")) { // Battery condition
            baseScore = count * 30.0;
        } else if (pattern.contains("Q3")) { // Physical damage
            baseScore = count * 40.0;
        }

        return baseScore * getModelMultiplier(model);
    }

    /**
     * Calculate market trend adjustments
     */
    private double calculateMarketTrendAdjustment(Model model) {
        // Get recent price history
        List<PriceSnapshot> recentPrices = priceSnapshotRepository
                .findTop10ByModelOrderByCreatedAtDesc(model);
        
        if (recentPrices.isEmpty()) {
            return 0.0;
        }

        // Calculate trend direction and magnitude
        double trendAdjustment = calculateTrend(recentPrices);
        
        // Apply market sentiment analysis
        double sentimentAdjustment = calculateMarketSentiment(model);
        
        return trendAdjustment + sentimentAdjustment;
    }

    /**
     * Calculate price trend from historical data
     */
    private double calculateTrend(List<PriceSnapshot> prices) {
        if (prices.size() < 2) {
            return 0.0;
        }

        double firstPrice = prices.get(prices.size() - 1).getFinalPrice();
        double lastPrice = prices.get(0).getFinalPrice();
        
        double trendPercentage = ((lastPrice - firstPrice) / firstPrice) * 100;
        
        // Apply trend dampening for stability
        return lastPrice * (trendPercentage / 100) * 0.1; // 10% of trend impact
    }

    /**
     * Calculate market sentiment adjustment
     */
    private double calculateMarketSentiment(Model model) {
        // Get sentiment from recent orders
        LocalDateTime oneWeekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        
        List<Order> recentOrders = orderRepository
                .findByModelAndCreatedAtAfter(model, oneWeekAgo);
        
        if (recentOrders.isEmpty()) {
            return 0.0;
        }

        // Calculate average offer acceptance rate
        double acceptanceRate = calculateAcceptanceRate(recentOrders);
        
        // Adjust price based on demand
        if (acceptanceRate > 0.8) { // High demand
            return model.getBasePrice() * 0.05; // 5% increase
        } else if (acceptanceRate < 0.5) { // Low demand
            return -model.getBasePrice() * 0.03; // 3% decrease
        }

        return 0.0;
    }

    /**
     * Calculate offer acceptance rate
     */
    private double calculateAcceptanceRate(List<Order> orders) {
        long totalOrders = orders.size();
        long acceptedOrders = orders.stream()
                .filter(order -> order.getStatus() == 2) // Accepted status
                .count();

        return totalOrders > 0 ? (double) acceptedOrders / totalOrders : 0.0;
    }

    /**
     * Calculate seasonal adjustments
     */
    private double calculateSeasonalAdjustment(Model model) {
        int currentMonth = LocalDateTime.now().getMonthValue();
        
        // Seasonal multipliers based on historical data
        double seasonalMultiplier = 1.0;
        
        switch (currentMonth) {
            case 1: case 2: // January, February - Low season
                seasonalMultiplier = 0.95;
                break;
            case 6: case 7: case 8: // Summer months - High season
                seasonalMultiplier = 1.05;
                break;
            case 11: case 12: // Holiday season - High demand
                seasonalMultiplier = 1.08;
                break;
        }

        return model.getBasePrice() * (seasonalMultiplier - 1.0);
    }

    /**
     * Calculate competitor price adjustments
     */
    private double calculateCompetitorAdjustment(Model model) {
        // Get competitor prices from market data
        List<Double> competitorPrices = getCompetitorPrices(model);
        
        if (competitorPrices.isEmpty()) {
            return 0.0;
        }

        double ourPrice = model.getBasePrice();
        double avgCompetitorPrice = competitorPrices.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(ourPrice);

        // Adjust our price to be competitive
        double difference = avgCompetitorPrice - ourPrice;
        
        // Apply competitive strategy
        if (difference > 0) {
            return difference * 0.8; // Match 80% of competitor advantage
        } else {
            return difference * 0.5; // Reduce impact of being higher priced
        }
    }

    /**
     * Get competitor prices (placeholder for market data integration)
     */
    private List<Double> getCompetitorPrices(Model model) {
        // In production, this would integrate with market data APIs
        // For now, return mock data based on model category
        List<Double> prices = new ArrayList<>();
        
        switch (model.getCategory().getName().toLowerCase()) {
            case "smartphone":
                prices.add(model.getBasePrice() * 0.95);
                prices.add(model.getBasePrice() * 1.02);
                prices.add(model.getBasePrice() * 0.98);
                break;
            case "laptop":
                prices.add(model.getBasePrice() * 0.90);
                prices.add(model.getBasePrice() * 1.05);
                prices.add(model.getBasePrice() * 0.97);
                break;
            default:
                prices.add(model.getBasePrice() * 0.98);
                prices.add(model.getBasePrice() * 1.01);
        }

        return prices;
    }

    /**
     * Calculate final AI-powered price
     */
    private double calculateFinalAiPrice(double basePrice, double conditionDeductions, 
                                       double marketAdjustment, double seasonalAdjustment, 
                                       double competitorAdjustment) {
        
        double finalPrice = basePrice - conditionDeductions + marketAdjustment + 
                           seasonalAdjustment + competitorAdjustment;

        // Ensure price doesn't go below minimum threshold
        double minimumPrice = basePrice * 0.3; // 30% of base price minimum
        return Math.max(finalPrice, minimumPrice);
    }

    /**
     * Generate detailed pricing breakdown
     */
    private Map<String, Object> generatePricingBreakdown(double basePrice, double conditionDeductions,
                                                       double marketAdjustment, double seasonalAdjustment,
                                                       double competitorAdjustment, double finalPrice) {
        
        Map<String, Object> breakdown = new HashMap<>();
        
        breakdown.put("basePrice", basePrice);
        breakdown.put("conditionDeductions", conditionDeductions);
        breakdown.put("marketAdjustment", marketAdjustment);
        breakdown.put("seasonalAdjustment", seasonalAdjustment);
        breakdown.put("competitorAdjustment", competitorAdjustment);
        breakdown.put("finalPrice", finalPrice);
        
        // Calculate percentages
        breakdown.put("conditionDeductionPercentage", 
                     (conditionDeductions / basePrice) * 100);
        breakdown.put("marketAdjustmentPercentage", 
                     (marketAdjustment / basePrice) * 100);
        breakdown.put("seasonalAdjustmentPercentage", 
                     (seasonalAdjustment / basePrice) * 100);
        breakdown.put("competitorAdjustmentPercentage", 
                     (competitorAdjustment / basePrice) * 100);
        
        // Calculate confidence score
        breakdown.put("pricingConfidence", calculatePricingConfidence(basePrice, finalPrice));
        
        // Add recommendation
        breakdown.put("recommendation", generatePricingRecommendation(finalPrice, basePrice));

        return breakdown;
    }

    /**
     * Calculate pricing confidence score
     */
    private double calculatePricingConfidence(double basePrice, double finalPrice) {
        double priceRatio = finalPrice / basePrice;
        
        // Higher confidence for prices closer to base price
        if (priceRatio >= 0.8 && priceRatio <= 1.2) {
            return 0.9; // High confidence
        } else if (priceRatio >= 0.6 && priceRatio <= 1.4) {
            return 0.7; // Medium confidence
        } else {
            return 0.5; // Low confidence
        }
    }

    /**
     * Generate pricing recommendation
     */
    private String generatePricingRecommendation(double finalPrice, double basePrice) {
        double priceRatio = finalPrice / basePrice;
        
        if (priceRatio >= 1.1) {
            return "EXCELLENT_VALUE - Price is significantly above market average";
        } else if (priceRatio >= 0.9) {
            return "GOOD_VALUE - Price is competitive and fair";
        } else if (priceRatio >= 0.7) {
            return "FAIR_VALUE - Price is reasonable for condition";
        } else {
            return "LOW_VALUE - Consider negotiating for better price";
        }
    }

    /**
     * Get condition weight for questions
     */
    private double getConditionWeight(Long questionId) {
        // Assign weights based on question importance
        Map<Long, Double> weights = new HashMap<>();
        weights.put(1L, 1.5); // Screen condition - high impact
        weights.put(2L, 1.2); // Battery condition - medium-high impact
        weights.put(3L, 1.0); // Physical damage - medium impact
        weights.put(4L, 0.8); // Accessories - low impact
        
        return weights.getOrDefault(questionId, 1.0);
    }

    /**
     * Get model-specific condition multiplier
     */
    private double getModelConditionMultiplier(Model model) {
        // Premium brands may have higher condition sensitivity
        if (model.getBrand().getName().toLowerCase().contains("apple") || 
            model.getBrand().getName().toLowerCase().contains("samsung")) {
            return 1.2;
        }
        return 1.0;
    }

    /**
     * Get model multiplier for ML scoring
     */
    private double getModelMultiplier(Model model) {
        // Premium models get higher ML scoring impact
        if (model.getBasePrice() > 50000) {
            return 1.5; // Premium model
        } else if (model.getBasePrice() > 20000) {
            return 1.2; // Mid-range model
        }
        return 1.0; // Budget model
    }

    /**
     * Get pricing history for a model
     */
    public List<Map<String, Object>> getPricingHistory(Long modelId) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Model not found"));

        List<PriceSnapshot> snapshots = priceSnapshotRepository
                .findByModelOrderByCreatedAtDesc(model);

        return snapshots.stream()
                .map(snapshot -> {
                    Map<String, Object> historyEntry = new HashMap<>();
                    historyEntry.put("date", snapshot.getCreatedAt());
                    historyEntry.put("price", snapshot.getFinalPrice());
                    historyEntry.put("reason", snapshot.getReason());
                    return historyEntry;
                })
                .collect(Collectors.toList());
    }

    /**
     * Predict future pricing trends
     */
    public Map<String, Object> predictPricingTrend(Long modelId, int days) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Model not found"));

        List<PriceSnapshot> historicalData = priceSnapshotRepository
                .findTop30ByModelOrderByCreatedAtDesc(model);

        if (historicalData.size() < 5) {
            return Map.of("error", "Insufficient data for prediction");
        }

        // Simple linear regression for trend prediction
        double[] prices = historicalData.stream()
                .mapToDouble(PriceSnapshot::getFinalPrice)
                .toArray();

        double trend = calculateLinearTrend(prices);
        double currentPrice = prices[0];
        double predictedPrice = currentPrice + (trend * days);

        Map<String, Object> prediction = new HashMap<>();
        prediction.put("currentPrice", currentPrice);
        prediction.put("predictedPrice", predictedPrice);
        prediction.put("trend", trend);
        prediction.put("days", days);
        prediction.put("confidence", calculatePredictionConfidence(prices));

        return prediction;
    }

    /**
     * Calculate linear trend using simple regression
     */
    private double calculateLinearTrend(double[] prices) {
        int n = prices.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += prices[i];
            sumXY += i * prices[i];
            sumXX += i * i;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        return slope;
    }

    /**
     * Calculate prediction confidence based on data variance
     */
    private double calculatePredictionConfidence(double[] prices) {
        double mean = Arrays.stream(prices).average().orElse(0);
        double variance = Arrays.stream(prices)
                .map(p -> Math.pow(p - mean, 2))
                .average()
                .orElse(0);

        double stdDev = Math.sqrt(variance);
        double coefficientOfVariation = stdDev / mean;

        // Lower coefficient of variation means higher confidence
        if (coefficientOfVariation < 0.1) {
            return 0.9; // High confidence
        } else if (coefficientOfVariation < 0.2) {
            return 0.7; // Medium confidence
        } else {
            return 0.5; // Low confidence
        }
    }
}