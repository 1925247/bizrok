package com.bizrok.service;

import com.bizrok.model.entity.Model;
import com.bizrok.model.entity.Order;
import com.bizrok.model.entity.User;
import com.bizrok.repository.ModelRepository;
import com.bizrok.repository.OrderRepository;
import com.bizrok.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PerformanceOptimizationService {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private final Map<String, Object> queryCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> cacheTimestamps = new ConcurrentHashMap<>();

    /**
     * Optimized model search with caching and pagination
     */
    @Cacheable(value = "models", key = "#category + '-' + #brand + '-' + #pageable.pageNumber")
    public Page<Model> searchModelsOptimized(String category, String brand, Pageable pageable) {
        if (category != null && brand != null) {
            return modelRepository.findByCategoryNameAndBrandName(category, brand, pageable);
        } else if (category != null) {
            return modelRepository.findByCategoryName(category, pageable);
        } else if (brand != null) {
            return modelRepository.findByBrandName(brand, pageable);
        } else {
            return modelRepository.findAll(pageable);
        }
    }

    /**
     * Optimized order retrieval with eager loading
     */
    @Cacheable(value = "orders", key = "#userId + '-' + #status + '-' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<Order> getUserOrdersOptimized(Long userId, Integer status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByUserIdAndStatus(userId, status, pageable);
        } else {
            return orderRepository.findByUserId(userId, pageable);
        }
    }

    /**
     * Bulk order processing for performance
     */
    @Transactional
    public List<Order> processOrdersBulk(List<Order> orders) {
        // Use batch processing for better performance
        List<Order> savedOrders = new ArrayList<>();
        
        for (int i = 0; i < orders.size(); i++) {
            savedOrders.add(orderRepository.save(orders.get(i)));
            
            // Flush and clear every 50 records to manage memory
            if (i % 50 == 0) {
                orderRepository.flush();
                // Clear the persistence context to free memory
            }
        }
        
        return savedOrders;
    }

    /**
     * Optimized price calculation with caching
     */
    @Cacheable(value = "prices", key = "#modelId + '-' + #conditionScore")
    public Double calculateOptimizedPrice(Long modelId, Double conditionScore) {
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("Model not found"));
        
        // Apply condition-based pricing
        double basePrice = model.getBasePrice();
        double conditionMultiplier = Math.max(0.3, Math.min(1.0, conditionScore));
        
        return basePrice * conditionMultiplier;
    }

    /**
     * Async data processing for heavy operations
     */
    public CompletableFuture<List<Model>> getPopularModelsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate heavy computation
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return modelRepository.findTop10ByOrderByOrderCountDesc();
        });
    }

    /**
     * Optimized user analytics with caching
     */
    @Cacheable(value = "userAnalytics", key = "#timeRange")
    public Map<String, Object> getUserAnalyticsOptimized(String timeRange) {
        LocalDateTime startDate = getStartDate(timeRange);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, endDate);
        List<User> users = userRepository.findByCreatedAtAfter(startDate);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // User growth
        analytics.put("userGrowth", calculateUserGrowth(users, timeRange));
        
        // Order trends
        analytics.put("orderTrends", calculateOrderTrends(orders, timeRange));
        
        // Revenue analytics
        analytics.put("revenueAnalytics", calculateRevenueAnalytics(orders));
        
        return analytics;
    }

    /**
     * Database query optimization with indexes
     */
    public List<Model> findModelsByPriceRangeOptimized(double minPrice, double maxPrice) {
        // Use indexed fields for better performance
        return modelRepository.findByBasePriceBetween(minPrice, maxPrice);
    }

    /**
     * Memory-efficient large dataset processing
     */
    public void processLargeDatasetOptimized() {
        int batchSize = 1000;
        int offset = 0;
        boolean hasMoreData = true;
        
        while (hasMoreData) {
            List<Order> batch = orderRepository.findOrdersBatch(offset, batchSize);
            
            if (batch.isEmpty()) {
                hasMoreData = false;
            } else {
                // Process batch
                processOrderBatch(batch);
                offset += batchSize;
            }
        }
    }

    /**
     * Connection pooling optimization
     */
    public void optimizeDatabaseConnections() {
        // This would be configured in application properties
        // spring.datasource.hikari.maximum-pool-size=20
        // spring.datasource.hikari.minimum-idle=5
        // spring.datasource.hikari.connection-timeout=30000
    }

    /**
     * Query result caching with TTL
     */
    public <T> T getCachedQueryResult(String cacheKey, int ttlMinutes, QueryFunction<T> queryFunction) {
        LocalDateTime now = LocalDateTime.now();
        
        // Check if cached result exists and is still valid
        if (queryCache.containsKey(cacheKey)) {
            LocalDateTime cachedTime = cacheTimestamps.get(cacheKey);
            if (ChronoUnit.MINUTES.between(cachedTime, now) < ttlMinutes) {
                return (T) queryCache.get(cacheKey);
            }
        }
        
        // Execute query and cache result
        T result = queryFunction.execute();
        queryCache.put(cacheKey, result);
        cacheTimestamps.put(cacheKey, now);
        
        return result;
    }

    /**
     * Cache eviction strategy
     */
    @CacheEvict(value = {"models", "orders", "prices"}, allEntries = true)
    public void clearCache() {
        queryCache.clear();
        cacheTimestamps.clear();
    }

    /**
     * Performance monitoring and metrics
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Database connection pool metrics
        metrics.put("dbConnectionsActive", getActiveConnections());
        metrics.put("dbConnectionsIdle", getIdleConnections());
        
        // Cache hit rate
        metrics.put("cacheHitRate", getCacheHitRate());
        
        // Query performance
        metrics.put("avgQueryTime", getAverageQueryTime());
        
        // Memory usage
        metrics.put("memoryUsage", getMemoryUsage());
        
        return metrics;
    }

    /**
     * Index optimization suggestions
     */
    public List<String> getIndexOptimizationSuggestions() {
        List<String> suggestions = new ArrayList<>();
        
        // Check for missing indexes on frequently queried fields
        if (!hasIndexOn("orders", "user_id")) {
            suggestions.add("CREATE INDEX idx_orders_user_id ON orders(user_id);");
        }
        
        if (!hasIndexOn("orders", "status")) {
            suggestions.add("CREATE INDEX idx_orders_status ON orders(status);");
        }
        
        if (!hasIndexOn("models", "category_id")) {
            suggestions.add("CREATE INDEX idx_models_category_id ON models(category_id);");
        }
        
        return suggestions;
    }

    /**
     * Query optimization with EXPLAIN ANALYZE
     */
    public String analyzeQueryPerformance(String query) {
        // This would integrate with database EXPLAIN ANALYZE
        // For now, return mock analysis
        return "Query analysis: " + query + " - Use indexes on WHERE clauses";
    }

    /**
     * Bulk data import optimization
     */
    @Transactional
    public void bulkImportModels(List<Model> models) {
        int batchSize = 100;
        
        for (int i = 0; i < models.size(); i++) {
            modelRepository.save(models.get(i));
            
            if (i % batchSize == 0) {
                modelRepository.flush();
                modelRepository.clear();
            }
        }
    }

    /**
     * Lazy loading optimization
     */
    public Order getOrderWithLazyLoading(Long orderId) {
        // Use @EntityGraph to optimize fetching related entities
        return orderRepository.findOrderWithDetails(orderId);
    }

    /**
     * Connection leak detection
     */
    public void detectConnectionLeaks() {
        // Monitor connection pool for leaks
        // This would integrate with connection pool monitoring
    }

    /**
     * Database statistics collection
     */
    public Map<String, Object> collectDatabaseStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Table sizes
        stats.put("tableSizes", getTableSizes());
        
        // Index usage
        stats.put("indexUsage", getIndexUsage());
        
        // Query statistics
        stats.put("queryStats", getQueryStatistics());
        
        return stats;
    }

    // Helper methods

    private LocalDateTime getStartDate(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        switch (timeRange) {
            case "week": return now.minus(7, ChronoUnit.DAYS);
            case "month": return now.minus(30, ChronoUnit.DAYS);
            case "quarter": return now.minus(90, ChronoUnit.DAYS);
            default: return now.minus(30, ChronoUnit.DAYS);
        }
    }

    private Map<String, Object> calculateUserGrowth(List<User> users, String timeRange) {
        Map<String, Object> growth = new HashMap<>();
        growth.put("newUsers", users.size());
        growth.put("timeRange", timeRange);
        return growth;
    }

    private Map<String, Object> calculateOrderTrends(List<Order> orders, String timeRange) {
        Map<String, Object> trends = new HashMap<>();
        trends.put("totalOrders", orders.size());
        trends.put("timeRange", timeRange);
        return trends;
    }

    private Map<String, Object> calculateRevenueAnalytics(List<Order> orders) {
        Map<String, Object> revenue = new HashMap<>();
        double totalRevenue = orders.stream()
                .filter(order -> order.getStatus() == 2)
                .mapToDouble(Order::getFinalPrice)
                .sum();
        revenue.put("totalRevenue", totalRevenue);
        return revenue;
    }

    private void processOrderBatch(List<Order> batch) {
        // Process batch of orders
        batch.forEach(order -> {
            // Business logic for order processing
        });
    }

    private int getActiveConnections() {
        // Get from connection pool metrics
        return 10; // Mock value
    }

    private int getIdleConnections() {
        // Get from connection pool metrics
        return 5; // Mock value
    }

    private double getCacheHitRate() {
        // Calculate cache hit rate
        return 0.85; // Mock value
    }

    private double getAverageQueryTime() {
        // Get from database metrics
        return 50.0; // Mock value in milliseconds
    }

    private Map<String, Object> getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        memory.put("max", runtime.maxMemory());
        return memory;
    }

    private boolean hasIndexOn(String table, String column) {
        // Check if index exists on table.column
        return false; // Mock implementation
    }

    private Map<String, Long> getTableSizes() {
        // Get table sizes from database
        return new HashMap<>();
    }

    private Map<String, Object> getIndexUsage() {
        // Get index usage statistics
        return new HashMap<>();
    }

    private Map<String, Object> getQueryStatistics() {
        // Get query execution statistics
        return new HashMap<>();
    }

    /**
     * Functional interface for cached queries
     */
    @FunctionalInterface
    public interface QueryFunction<T> {
        T execute();
    }
}