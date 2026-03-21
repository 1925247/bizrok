package main.java.com.bizrok.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * OTP Utility class for generating and validating OTP codes
 */
@Component
public class OtpUtil {
    
    private static final String OTP_CHARACTERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Generate a random OTP code
     */
    public String generateOtp(int length) {
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(OTP_CHARACTERS.charAt(random.nextInt(OTP_CHARACTERS.length())));
        }
        return otp.toString();
    }
    
    /**
     * Validate OTP format
     */
    public boolean isValidOtp(String otp) {
        if (otp == null || otp.isEmpty()) {
            return false;
        }
        
        // Check if OTP contains only digits
        return otp.matches("\\d+");
    }
    
    /**
     * Validate OTP length
     */
    public boolean isValidOtpLength(String otp, int expectedLength) {
        return otp != null && otp.length() == expectedLength;
    }
}