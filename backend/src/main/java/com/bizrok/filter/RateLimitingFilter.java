package com.bizrok.filter;

import com.bizrok.config.RateLimitingConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIpAddress(request);
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();

        // Skip rate limiting for certain paths
        if (shouldSkipRateLimiting(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if it's a login request
        boolean isLoginRequest = "/api/auth/login".equals(requestPath) && "POST".equals(requestMethod);

        String redisKey = generateRedisKey(clientIp, requestPath, requestMethod, isLoginRequest);
        long currentTime = Instant.now().toEpochMilli();

        // Get current window start time
        long windowStart = isLoginRequest ? 
            currentTime - RateLimitingConfig.RateLimitConfig.LOGIN_WINDOW_SIZE.toMillis() :
            currentTime - RateLimitingConfig.RateLimitConfig.WINDOW_SIZE.toMillis();

        // Clean up old entries
        cleanupOldEntries(redisKey, windowStart);

        // Get current request count
        Long currentCount = getCurrentRequestCount(redisKey, currentTime, 
            isLoginRequest ? RateLimitingConfig.RateLimitConfig.LOGIN_WINDOW_SIZE : RateLimitingConfig.RateLimitConfig.WINDOW_SIZE);

        // Check if rate limit exceeded
        if (currentCount != null && currentCount >= 
            (isLoginRequest ? RateLimitingConfig.RateLimitConfig.LOGIN_ATTEMPTS_PER_WINDOW : RateLimitingConfig.RateLimitConfig.REQUESTS_PER_WINDOW)) {
            
            // Check if user is blocked
            if (isUserBlocked(redisKey + ":blocked")) {
                sendRateLimitExceededResponse(response, "Too many requests. Account temporarily blocked.");
                return;
            }

            // Block user
            blockUser(redisKey + ":blocked");
            sendRateLimitExceededResponse(response, "Too many failed login attempts. Account blocked for 1 hour.");
            return;
        }

        // Allow request and increment counter
        incrementRequestCount(redisKey, currentTime);
        
        // Add rate limit headers
        addRateLimitHeaders(response, currentCount != null ? currentCount + 1 : 1, 
            isLoginRequest ? RateLimitingConfig.RateLimitConfig.LOGIN_ATTEMPTS_PER_WINDOW : RateLimitingConfig.RateLimitConfig.REQUESTS_PER_WINDOW);

        filterChain.doFilter(request, response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private boolean shouldSkipRateLimiting(String requestPath) {
        return requestPath.startsWith("/api/health") || 
               requestPath.startsWith("/actuator") ||
               requestPath.startsWith("/swagger") ||
               requestPath.startsWith("/v3/api-docs");
    }

    private String generateRedisKey(String clientIp, String requestPath, String requestMethod, boolean isLoginRequest) {
        if (isLoginRequest) {
            return "rate_limit:login:" + clientIp;
        }
        return "rate_limit:" + clientIp + ":" + requestPath + ":" + requestMethod;
    }

    private void cleanupOldEntries(String redisKey, long windowStart) {
        try {
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
        } catch (Exception e) {
            // Log error but don't fail the request
            logger.warn("Failed to cleanup old rate limit entries", e);
        }
    }

    private Long getCurrentRequestCount(String redisKey, long currentTime, java.time.Duration windowSize) {
        try {
            long windowStart = currentTime - windowSize.toMillis();
            return redisTemplate.opsForZSet().count(redisKey, windowStart, currentTime);
        } catch (Exception e) {
            logger.warn("Failed to get current request count", e);
            return 0L;
        }
    }

    private boolean isUserBlocked(String blockKey) {
        try {
            return redisTemplate.hasKey(blockKey);
        } catch (Exception e) {
            logger.warn("Failed to check if user is blocked", e);
            return false;
        }
    }

    private void blockUser(String blockKey) {
        try {
            redisTemplate.opsForValue().set(blockKey, "blocked", RateLimitingConfig.RateLimitConfig.BLOCK_DURATION);
        } catch (Exception e) {
            logger.warn("Failed to block user", e);
        }
    }

    private void incrementRequestCount(String redisKey, long currentTime) {
        try {
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(currentTime), currentTime);
            redisTemplate.expire(redisKey, RateLimitingConfig.RateLimitConfig.WINDOW_SIZE.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.warn("Failed to increment request count", e);
        }
    }

    private void addRateLimitHeaders(HttpServletResponse response, long currentCount, int limit) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - currentCount)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + RateLimitingConfig.RateLimitConfig.WINDOW_SIZE.toMillis()));
    }

    private void sendRateLimitExceededResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Rate limit exceeded");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}