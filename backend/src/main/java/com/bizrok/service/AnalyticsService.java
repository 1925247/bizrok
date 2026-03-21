package com.bizrok.service;

import com.bizrok.model.entity.Model;
import com.bizrok.model.entity.Order;
import com.bizrok.model.entity.User;
import com.bizrok.model.entity.PriceSnapshot;
import com.bizrok.repository.ModelRepository;
import com.bizrok.repository.OrderRepository;
import com.bizrok.repository.UserRepository;
import com.bizrok.repository.PriceSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PriceSnapshotRepository priceSnapshotRepository;

    /**
     * Get comprehensive business dashboard metrics
     */
    public Map<String, Object> getBusinessDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minus(1, ChronoUnit.MONTH);
        LocalDateTime oneWeekAgo = now.minus(7, ChronoUnit.DAYS);

        Map<String, Object> dashboard = new HashMap<>();

        // Revenue metrics
        dashboard.put("revenueMetrics", getRevenueMetrics(oneMonthAgo, now));
        
        // Order metrics
        dashboard.put("orderMetrics", getOrderMetrics(oneMonthAgo, now));
        
        // User metrics
        dashboard.put("userMetrics", getUserMetrics(oneMonthAgo, now));
        
        // Model performance
        dashboard.put("modelPerformance", getModelPerformance());
        
        // Geographic analysis
        dashboard.put("geographicAnalysis", getGeographicAnalysis());
        
        // Time-based trends
        dashboard.put("timeTrends", getTimeTrends(oneMonthAgo, now));

        return dashboard;
    }

    /**
     * Calculate revenue metrics
     */
    private Map<String, Object> getRevenueMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Total revenue
        double totalRevenue = orders.stream()
                .filter(order -> order.getStatus() == 2) // Completed orders
                .mapToDouble(Order::getFinalPrice)
                .sum();
        
        // Average order value
        double avgOrderValue = orders.stream()
                .filter(order -> order.getStatus() == 2)
                .mapToDouble(Order::getFinalPrice)
                .average()
                .orElse(0.0);
        
        // Revenue by status
        Map<Integer, Double> revenueByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                    Order::getStatus,
                    Collectors.summingDouble(Order::getFinalPrice)
                ));

        metrics.put("totalRevenue", totalRevenue);
        metrics.put("avgOrderValue", avgOrderValue);
        metrics.put("revenueByStatus", revenueByStatus);
        metrics.put("orderCount", orders.size());

        return metrics;
    }

    /**
     * Calculate order metrics
     */
    private Map<String, Object> getOrderMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Order count by status
        Map<Integer, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                    Order::getStatus,
                    Collectors.counting()
                ));
        
        // Conversion rate
        long totalOrders = orders.size();
        long completedOrders = ordersByStatus.getOrDefault(2, 0L); // Status 2 = Completed
        double conversionRate = totalOrders > 0 ? (double) completedOrders / totalOrders : 0.0;
        
        // Average processing time
        double avgProcessingTime = orders.stream()
                .filter(order -> order.getStatus() == 2 && order.getUpdatedAt() != null)
                .mapToLong(order -> ChronoUnit.HOURS.between(
                    order.getCreatedAt(), 
                    order.getUpdatedAt()
                ))
                .average()
                .orElse(0.0);

        metrics.put("ordersByStatus", ordersByStatus);
        metrics.put("conversionRate", conversionRate);
        metrics.put("avgProcessingTime", avgProcessingTime);
        metrics.put("totalOrders", totalOrders);

        return metrics;
    }

    /**
     * Calculate user metrics
     */
    private Map<String, Object> getUserMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        List<User> users = userRepository.findByCreatedAtAfter(startDate);
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        Map<String, Object> metrics = new HashMap<>();
        
        // New users
        long newUsers = users.size();
        
        // Active users (users with orders in period)
        Set<Long> activeUserIds = orders.stream()
                .map(Order::getUserId)
                .collect(Collectors.toSet());
        long activeUsers = activeUserIds.size();
        
        // User retention (simplified calculation)
        double retentionRate = calculateRetentionRate();
        
        // Average orders per user
        double avgOrdersPerUser = activeUsers > 0 ? 
            (double) orders.size() / activeUsers : 0.0;

        metrics.put("newUsers", newUsers);
        metrics.put("activeUsers", activeUsers);
        metrics.put("retentionRate", retentionRate);
        metrics.put("avgOrdersPerUser", avgOrdersPerUser);

        return metrics;
    }

    /**
     * Calculate user retention rate
     */
    private double calculateRetentionRate() {
        // Get all users
        List<User> allUsers = userRepository.findAll();
        
        // Get users with multiple orders
        Map<Long, Long> userOrderCounts = orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    Order::getUserId,
                    Collectors.counting()
                ));
        
        long returningUsers = userOrderCounts.values().stream()
                .filter(count -> count > 1)
                .count();

        return allUsers.size() > 0 ? (double) returningUsers / allUsers.size() : 0.0;
    }

    /**
     * Get model performance analysis
     */
    private Map<String, Object> getModelPerformance() {
        List<Model> models = modelRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        
        Map<String, Object> performance = new HashMap<>();
        
        // Top performing models by order count
        Map<Long, Long> modelOrderCounts = orders.stream()
                .collect(Collectors.groupingBy(
                    Order::getModelId,
                    Collectors.counting()
                ));
        
        List<Map<String, Object>> topModels = modelOrderCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Model model = modelRepository.findById(entry.getKey()).orElse(null);
                    Map<String, Object> modelData = new HashMap<>();
                    modelData.put("model", model != null ? model.getName() : "Unknown");
                    modelData.put("brand", model != null ? model.getBrand().getName() : "Unknown");
                    modelData.put("orderCount", entry.getValue());
                    return modelData;
                })
                .collect(Collectors.toList());
        
        // Average price by category
        Map<String, Double> avgPriceByCategory = orders.stream()
                .filter(order -> order.getStatus() == 2)
                .collect(Collectors.groupingBy(
                    order -> {
                        Model model = modelRepository.findById(order.getModelId()).orElse(null);
                        return model != null ? model.getCategory().getName() : "Unknown";
                    },
                    Collectors.averagingDouble(Order::getFinalPrice)
                ));

        performance.put("topModels", topModels);
        performance.put("avgPriceByCategory", avgPriceByCategory);

        return performance;
    }

    /**
     * Get geographic analysis
     */
    private Map<String, Object> getGeographicAnalysis() {
        List<Order> orders = orderRepository.findAll();
        
        Map<String, Object> geoAnalysis = new HashMap<>();
        
        // Orders by state
        Map<String, Long> ordersByState = orders.stream()
                .filter(order -> order.getState() != null)
                .collect(Collectors.groupingBy(
                    Order::getState,
                    Collectors.counting()
                ));
        
        // Orders by city
        Map<String, Long> ordersByCity = orders.stream()
                .filter(order -> order.getCity() != null)
                .collect(Collectors.groupingBy(
                    Order::getCity,
                    Collectors.counting()
                ));
        
        // Revenue by state
        Map<String, Double> revenueByState = orders.stream()
                .filter(order -> order.getStatus() == 2 && order.getState() != null)
                .collect(Collectors.groupingBy(
                    Order::getState,
                    Collectors.summingDouble(Order::getFinalPrice)
                ));

        geoAnalysis.put("ordersByState", ordersByState);
        geoAnalysis.put("ordersByCity", ordersByCity);
        geoAnalysis.put("revenueByState", revenueByState);

        return geoAnalysis;
    }

    /**
     * Get time-based trends
     */
    private Map<String, Object> getTimeTrends(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        
        Map<String, Object> trends = new HashMap<>();
        
        // Daily order trends
        Map<String, Long> dailyOrders = orders.stream()
                .collect(Collectors.groupingBy(
                    order -> order.getCreatedAt().toLocalDate().toString(),
                    Collectors.counting()
                ));
        
        // Hourly order patterns
        Map<Integer, Long> hourlyOrders = orders.stream()
                .collect(Collectors.groupingBy(
                    order -> order.getCreatedAt().getHour(),
                    Collectors.counting()
                ));
        
        // Weekly revenue trends
        Map<String, Double> weeklyRevenue = orders.stream()
                .filter(order -> order.getStatus() == 2)
                .collect(Collectors.groupingBy(
                    order -> {
                        LocalDateTime date = order.getCreatedAt();
                        int week = date.get(ChronoUnit.WEEKS);
                        return "Week " + week;
                    },
                    Collectors.summingDouble(Order::getFinalPrice)
                ));

        trends.put("dailyOrders", dailyOrders);
        trends.put("hourlyOrders", hourlyOrders);
        trends.put("weeklyRevenue", weeklyRevenue);

        return trends;
    }

    /**
     * Get detailed model analytics
     */
    public Map<String, Object> getModelAnalytics(Long modelId) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Model not found"));

        List<Order> modelOrders = orderRepository.findByModelId(modelId);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Basic model metrics
        analytics.put("modelName", model.getName());
        analytics.put("basePrice", model.getBasePrice());
        analytics.put("totalOrders", modelOrders.size());
        
        // Price analysis
        analytics.put("priceAnalysis", getPriceAnalysis(modelOrders));
        
        // Condition analysis
        analytics.put("conditionAnalysis", getConditionAnalysis(modelOrders));
        
        // Time analysis
        analytics.put("timeAnalysis", getTimeAnalysis(modelOrders));

        return analytics;
    }

    /**
     * Analyze pricing patterns for a model
     */
    private Map<String, Object> getPriceAnalysis(List<Order> orders) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (orders.isEmpty()) {
            return analysis;
        }

        // Price statistics
        double minPrice = orders.stream().mapToDouble(Order::getFinalPrice).min().orElse(0);
        double maxPrice = orders.stream().mapToDouble(Order::getFinalPrice).max().orElse(0);
        double avgPrice = orders.stream().mapToDouble(Order::getFinalPrice).average().orElse(0);
        
        // Price distribution
        Map<String, Long> priceRanges = orders.stream()
                .collect(Collectors.groupingBy(
                    order -> {
                        double price = order.getFinalPrice();
                        if (price < 10000) return "Under 10K";
                        else if (price < 25000) return "10K-25K";
                        else if (price < 50000) return "25K-50K";
                        else return "Above 50K";
                    },
                    Collectors.counting()
                ));

        analysis.put("minPrice", minPrice);
        analysis.put("maxPrice", maxPrice);
        analysis.put("avgPrice", avgPrice);
        analysis.put("priceRanges", priceRanges);

        return analysis;
    }

    /**
     * Analyze condition patterns for a model
     */
    private Map<String, Object> getConditionAnalysis(List<Order> orders) {
        Map<String, Object> analysis = new HashMap<>();
        
        // This would need to be enhanced with actual condition data
        // For now, using order status as a proxy
        
        Map<Integer, Long> statusDistribution = orders.stream()
                .collect(Collectors.groupingBy(
                    Order::getStatus,
                    Collectors.counting()
                ));

        analysis.put("statusDistribution", statusDistribution);
        
        // Average processing time by status
        Map<Integer, Double> avgProcessingTime = orders.stream()
                .filter(order -> order.getUpdatedAt() != null)
                .collect(Collectors.groupingBy(
                    Order::getStatus,
                    Collectors.averagingDouble(order -> 
                        ChronoUnit.HOURS.between(order.getCreatedAt(), order.getUpdatedAt()))
                ));

        analysis.put("avgProcessingTime", avgProcessingTime);

        return analysis;
    }

    /**
     * Analyze time patterns for a model
     */
    private Map<String, Object> getTimeAnalysis(List<Order> orders) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Monthly trends
        Map<String, Long> monthlyOrders = orders.stream()
                .collect(Collectors.groupingBy(
                    order -> order.getCreatedAt().getYear() + "-" + order.getCreatedAt().getMonthValue(),
                    Collectors.counting()
                ));
        
        // Day of week patterns
        Map<Integer, Long> dayOfWeekOrders = orders.stream()
                .collect(Collectors.groupingBy(
                    order -> order.getCreatedAt().getDayOfWeek().getValue(),
                    Collectors.counting()
                ));

        analysis.put("monthlyOrders", monthlyOrders);
        analysis.put("dayOfWeekOrders", dayOfWeekOrders);

        return analysis;
    }

    /**
     * Get user behavior analytics
     */
    public Map<String, Object> getUserBehaviorAnalytics() {
        List<User> users = userRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        
        Map<String, Object> analytics = new HashMap<>();
        
        // User segmentation
        analytics.put("userSegments", getUserSegments(users, orders));
        
        // Churn analysis
        analytics.put("churnAnalysis", getChurnAnalysis(users, orders));
        
        // Lifetime value analysis
        analytics.put("lifetimeValue", getLifetimeValueAnalysis(orders));

        return analytics;
    }

    /**
     * Segment users based on behavior
     */
    private Map<String, Object> getUserSegments(List<User> users, List<Order> orders) {
        Map<String, Object> segments = new HashMap<>();
        
        // Calculate order frequency for each user
        Map<Long, Long> userOrderCounts = orders.stream()
                .collect(Collectors.groupingBy(
                    Order::getUserId,
                    Collectors.counting()
                ));
        
        // Segment users
        long frequentUsers = userOrderCounts.values().stream()
                .filter(count -> count >= 3)
                .count();
        
        long occasionalUsers = userOrderCounts.values().stream()
                .filter(count -> count == 2)
                .count();
        
        long oneTimeUsers = userOrderCounts.values().stream()
                .filter(count -> count == 1)
                .count();
        
        long inactiveUsers = users.size() - userOrderCounts.size();

        segments.put("frequentUsers", frequentUsers);
        segments.put("occasionalUsers", occasionalUsers);
        segments.put("oneTimeUsers", oneTimeUsers);
        segments.put("inactiveUsers", inactiveUsers);

        return segments;
    }

    /**
     * Analyze user churn
     */
    private Map<String, Object> getChurnAnalysis(List<User> users, List<Order> orders) {
        Map<String, Object> churn = new HashMap<>();
        
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minus(6, ChronoUnit.MONTHS);
        
        // Users with orders in the last 6 months
        Set<Long> activeUsers = orders.stream()
                .filter(order -> order.getCreatedAt().isAfter(sixMonthsAgo))
                .map(Order::getUserId)
                .collect(Collectors.toSet());
        
        // Users who haven't ordered in 6 months
        Set<Long> churnedUsers = orders.stream()
                .filter(order -> order.getCreatedAt().isBefore(sixMonthsAgo))
                .map(Order::getUserId)
                .filter(userId -> !activeUsers.contains(userId))
                .collect(Collectors.toSet());
        
        double churnRate = users.size() > 0 ? 
            (double) churnedUsers.size() / users.size() : 0.0;

        churn.put("churnedUsers", churnedUsers.size());
        churn.put("churnRate", churnRate);
        churn.put("activeUsers", activeUsers.size());

        return churn;
    }

    /**
     * Calculate customer lifetime value
     */
    private Map<String, Object> getLifetimeValueAnalysis(List<Order> orders) {
        Map<String, Object> ltv = new HashMap<>();
        
        // Calculate total revenue per user
        Map<Long, Double> userRevenue = orders.stream()
                .filter(order -> order.getStatus() == 2)
                .collect(Collectors.groupingBy(
                    Order::getUserId,
                    Collectors.summingDouble(Order::getFinalPrice)
                ));
        
        // Calculate average LTV
        double avgLTV = userRevenue.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        // LTV distribution
        Map<String, Long> ltvRanges = userRevenue.values().stream()
                .collect(Collectors.groupingBy(
                    revenue -> {
                        if (revenue < 10000) return "Under 10K";
                        else if (revenue < 50000) return "10K-50K";
                        else if (revenue < 100000) return "50K-1L";
                        else return "Above 1L";
                    },
                    Collectors.counting()
                ));

        ltv.put("avgLTV", avgLTV);
        ltv.put("ltvRanges", ltvRanges);
        ltv.put("totalCustomers", userRevenue.size());

        return ltv;
    }

    /**
     * Get real-time dashboard metrics
     */
    public Map<String, Object> getRealTimeMetrics() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Recent orders
        long recentOrders = orderRepository.countByCreatedAtAfter(oneHourAgo);
        
        // Recent revenue
        double recentRevenue = orderRepository.findByCreatedAtAfter(oneHourAgo).stream()
                .filter(order -> order.getStatus() == 2)
                .mapToDouble(Order::getFinalPrice)
                .sum();
        
        // Active users (users who placed orders in last hour)
        long activeUsers = orderRepository.findByCreatedAtAfter(oneHourAgo).stream()
                .map(Order::getUserId)
                .distinct()
                .count();

        metrics.put("recentOrders", recentOrders);
        metrics.put("recentRevenue", recentRevenue);
        metrics.put("activeUsers", activeUsers);
        metrics.put("timestamp", LocalDateTime.now());

        return metrics;
    }
}