package main.java.com.bizrok.service;

import main.java.com.bizrok.model.entity.Settings;
import main.java.com.bizrok.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Settings Service for config-driven system
 * Manages system configuration without code changes
 */
@Service
public class SettingsService {
    
    @Autowired
    private SettingsRepository settingsRepository;
    
    /**
     * Get setting by key
     */
    public Optional<Settings> getSetting(String key) {
        return settingsRepository.findByKey(key);
    }
    
    /**
     * Get setting value as string
     */
    public String getStringValue(String key, String defaultValue) {
        return getSetting(key)
                .map(Settings::getValue)
                .orElse(defaultValue);
    }
    
    /**
     * Get setting value as boolean
     */
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        return getSetting(key)
                .map(Settings::getBooleanValue)
                .orElse(defaultValue);
    }
    
    /**
     * Get setting value as integer
     */
    public Integer getIntegerValue(String key, Integer defaultValue) {
        return getSetting(key)
                .map(Settings::getIntegerValue)
                .orElse(defaultValue);
    }
    
    /**
     * Get setting value as double
     */
    public Double getDoubleValue(String key, Double defaultValue) {
        return getSetting(key)
                .map(Settings::getDoubleValue)
                .orElse(defaultValue);
    }
    
    /**
     * Update setting
     */
    public Settings updateSetting(String key, String value) {
        Optional<Settings> existingSetting = settingsRepository.findByKey(key);
        
        Settings setting;
        if (existingSetting.isPresent()) {
            setting = existingSetting.get();
            setting.setValue(value);
        } else {
            setting = Settings.builder()
                    .key(key)
                    .value(value)
                    .isActive(true)
                    .build();
        }
        
        return settingsRepository.save(setting);
    }
    
    /**
     * Get all active settings
     */
    public List<Settings> getAllSettings() {
        return settingsRepository.findByIsActiveTrue();
    }
    
    /**
     * Enable/disable setting
     */
    public Settings toggleSetting(String key, boolean isActive) {
        Optional<Settings> settingOpt = settingsRepository.findByKey(key);
        
        if (settingOpt.isPresent()) {
            Settings setting = settingOpt.get();
            setting.setActive(isActive);
            return settingsRepository.save(setting);
        }
        
        throw new RuntimeException("Setting not found: " + key);
    }
    
    /**
     * Check if feature is enabled
     */
    public boolean isFeatureEnabled(String featureKey) {
        return getBooleanValue(featureKey, false);
    }
    
    /**
     * Get minimum price percentage
     */
    public Double getMinPricePercent() {
        return getDoubleValue("MIN_PRICE_PERCENT", 20.0);
    }
    
    /**
     * Get maximum deduction percentage
     */
    public Double getMaxDeductionPercent() {
        return getDoubleValue("MAX_DEDUCTION_PERCENT", 80.0);
    }
    
    /**
     * Check if KYC is enabled
     */
    public boolean isKycEnabled() {
        return isFeatureEnabled("ENABLE_KYC");
    }
    
    /**
     * Check if email OTP is enabled
     */
    public boolean isEmailOtpEnabled() {
        return isFeatureEnabled("ENABLE_EMAIL_OTP");
    }
    
    /**
     * Check if face match is enabled
     */
    public boolean isFaceMatchEnabled() {
        return isFeatureEnabled("ENABLE_FACE_MATCH");
    }
    
    /**
     * Check if bank check is enabled
     */
    public boolean isBankCheckEnabled() {
        return isFeatureEnabled("ENABLE_BANK_CHECK");
    }
    
    /**
     * Get OTP expiry time in minutes
     */
    public Integer getOtpExpiryMinutes() {
        return getIntegerValue("OTP_EXPIRY_MINUTES", 5);
    }
    
    /**
     * Get max OTP attempts
     */
    public Integer getMaxOtpAttempts() {
        return getIntegerValue("MAX_OTP_ATTEMPTS", 3);
    }
    
    /**
     * Get default order status
     */
    public String getDefaultOrderStatus() {
        return getStringValue("DEFAULT_ORDER_STATUS", "CREATED");
    }
    
    /**
     * Check if pincode validation is enabled
     */
    public boolean isPincodeValidationEnabled() {
        return isFeatureEnabled("ENABLE_PINCODE_VALIDATION");
    }
}