package com.bizrok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application class for Bizrok Device Buyback Platform
 * 
 * This application provides a config-driven device buyback platform with:
 * - Email OTP authentication
 * - Dynamic pricing engine
 * - Configurable question system
 * - Multi-role system (User, Admin, Partner, Field Executive)
 * - OCR and Face detection for KYC
 */
@SpringBootApplication
public class BizrokApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BizrokApplication.class, args);
    }
}