package com.bizrok.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger("AUDIT_LOGGER");
    
    @Autowired
    private ObjectMapper objectMapper;

    public void logUserAction(String action, String entityType, String entityId, Object details) {
        try {
            Map<String, Object> auditEvent = new HashMap<>();
            auditEvent.put("eventId", UUID.randomUUID().toString());
            auditEvent.put("timestamp", Instant.now().toString());
            auditEvent.put("action", action);
            auditEvent.put("entityType", entityType);
            auditEvent.put("entityId", entityId);
            auditEvent.put("user", getCurrentUser());
            auditEvent.put("ipAddress", getClientIpAddress());
            auditEvent.put("userAgent", getUserAgent());
            auditEvent.put("details", details);
            auditEvent.put("sessionId", getSessionId());
            
            logger.info("AUDIT: {}", objectMapper.writeValueAsString(auditEvent));
        } catch (Exception e) {
            logger.error("Failed to log audit event", e);
        }
    }

    public void logSecurityEvent(String eventType, String severity, String description, Object details) {
        try {
            Map<String, Object> securityEvent = new HashMap<>();
            securityEvent.put("eventId", UUID.randomUUID().toString());
            securityEvent.put("timestamp", Instant.now().toString());
            securityEvent.put("eventType", eventType);
            securityEvent.put("severity", severity);
            securityEvent.put("description", description);
            securityEvent.put("user", getCurrentUser());
            securityEvent.put("ipAddress", getClientIpAddress());
            securityEvent.put("userAgent", getUserAgent());
            securityEvent.put("details", details);
            
            logger.warn("SECURITY: {}", objectMapper.writeValueAsString(securityEvent));
        } catch (Exception e) {
            logger.error("Failed to log security event", e);
        }
    }

    public void logDataAccess(String dataType, String operation, String result) {
        try {
            Map<String, Object> dataEvent = new HashMap<>();
            dataEvent.put("eventId", UUID.randomUUID().toString());
            dataEvent.put("timestamp", Instant.now().toString());
            dataEvent.put("dataType", dataType);
            dataEvent.put("operation", operation);
            dataEvent.put("result", result);
            dataEvent.put("user", getCurrentUser());
            dataEvent.put("ipAddress", getClientIpAddress());
            dataEvent.put("sessionId", getSessionId());
            
            logger.info("DATA_ACCESS: {}", objectMapper.writeValueAsString(dataEvent));
        } catch (Exception e) {
            logger.error("Failed to log data access event", e);
        }
    }

    public void logKYCVerification(String documentType, String verificationResult, String confidence) {
        try {
            Map<String, Object> kycEvent = new HashMap<>();
            kycEvent.put("eventId", UUID.randomUUID().toString());
            kycEvent.put("timestamp", Instant.now().toString());
            kycEvent.put("eventType", "KYC_VERIFICATION");
            kycEvent.put("documentType", documentType);
            kycEvent.put("verificationResult", verificationResult);
            kycEvent.put("confidence", confidence);
            kycEvent.put("user", getCurrentUser());
            kycEvent.put("ipAddress", getClientIpAddress());
            
            logger.info("KYC: {}", objectMapper.writeValueAsString(kycEvent));
        } catch (Exception e) {
            logger.error("Failed to log KYC verification event", e);
        }
    }

    private String getCurrentUser() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    // In a real implementation, you would decode the JWT to get user info
                    return "user_from_jwt";
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "anonymous";
    }

    private String getClientIpAddress() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
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
        } catch (Exception e) {
            // Ignore
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            // Ignore
        }
        return "unknown";
    }

    private String getSessionId() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null && request.getSession(false) != null) {
                return request.getSession().getId();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "unknown";
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}