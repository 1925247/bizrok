package main.java.com.bizrok.service;

import main.java.com.bizrok.model.entity.EmailOtp;
import main.java.com.bizrok.model.entity.User;
import main.java.com.bizrok.repository.EmailOtpRepository;
import main.java.com.bizrok.repository.UserRepository;
import main.java.com.bizrok.util.OtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * OTP Service for email OTP authentication
 */
@Service
public class OtpService {
    
    @Autowired
    private EmailOtpRepository otpRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private OtpUtil otpUtil;
    
    @Value("${bizrok.otp.expiry}")
    private Long otpExpiryMinutes;
    
    @Value("${bizrok.otp.max-attempts}")
    private Integer maxAttempts;
    
    @Value("${bizrok.otp.length}")
    private Integer otpLength;
    
    /**
     * Send OTP to email
     */
    public void sendOtp(String email) {
        // Check if user exists, if not create one
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = User.builder()
                    .email(email)
                    .role(User.Role.USER)
                    .isActive(true)
                    .build();
            userRepository.save(user);
        }
        
        // Generate OTP
        String otp = otpUtil.generateOtp(otpLength);
        LocalDateTime expiryTime = LocalDateTime.now().plus(otpExpiryMinutes, ChronoUnit.MINUTES);
        
        // Save OTP
        EmailOtp emailOtp = EmailOtp.builder()
                .email(email)
                .otp(otp)
                .expiryTime(expiryTime)
                .attempts(0)
                .isVerified(false)
                .build();
        
        // Clean up old OTPs for this email
        cleanupOldOtps(email);
        
        otpRepository.save(emailOtp);
        
        // Send email
        sendEmail(email, otp);
    }
    
    /**
     * Verify OTP
     */
    public boolean verifyOtp(String email, String otp) {
        Optional<EmailOtp> otpOpt = otpRepository.findByEmailAndOtp(email, otp, LocalDateTime.now());
        
        if (otpOpt.isPresent()) {
            EmailOtp emailOtp = otpOpt.get();
            
            // Check if already verified
            if (emailOtp.getIsVerified()) {
                return false;
            }
            
            // Check if expired
            if (emailOtp.isExpired()) {
                return false;
            }
            
            // Mark as verified
            emailOtp.markAsVerified();
            otpRepository.save(emailOtp);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Check OTP attempts and expiry
     */
    public OtpStatus checkOtpStatus(String email) {
        Optional<EmailOtp> latestOtpOpt = otpRepository.findActiveOtpByEmail(email, LocalDateTime.now());
        
        if (latestOtpOpt.isPresent()) {
            EmailOtp otp = latestOtpOpt.get();
            
            if (otp.getIsVerified()) {
                return OtpStatus.VERIFIED;
            }
            
            if (otp.isExpired()) {
                return OtpStatus.EXPIRED;
            }
            
            if (otp.getAttempts() >= maxAttempts) {
                return OtpStatus.MAX_ATTEMPTS_REACHED;
            }
            
            return OtpStatus.VALID;
        }
        
        return OtpStatus.NOT_FOUND;
    }
    
    /**
     * Increment OTP attempts
     */
    public void incrementAttempts(String email) {
        Optional<EmailOtp> latestOtpOpt = otpRepository.findActiveOtpByEmail(email, LocalDateTime.now());
        
        if (latestOtpOpt.isPresent()) {
            EmailOtp otp = latestOtpOpt.get();
            otp.incrementAttempts();
            otpRepository.save(otp);
        }
    }
    
    /**
     * Clean up expired OTPs
     */
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOrVerifiedOtps(LocalDateTime.now());
    }
    
    /**
     * Check if OTP can be resent (time-based)
     */
    public boolean canResendOtp(String email) {
        List<EmailOtp> recentOtps = otpRepository.findByEmailAndCreatedAtAfter(
            email, 
            LocalDateTime.now().minus(1, ChronoUnit.MINUTES)
        );
        
        return recentOtps.size() < 3; // Max 3 OTPs per minute
    }
    
    private void cleanupOldOtps(String email) {
        List<EmailOtp> oldOtps = otpRepository.findByEmail(email);
        for (EmailOtp otp : oldOtps) {
            if (otp.isExpired() || otp.getIsVerified()) {
                otpRepository.delete(otp);
            }
        }
    }
    
    private void sendEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Bizrok OTP Code");
        message.setText("Your OTP code is: " + otp + "\n\nThis code will expire in " + otpExpiryMinutes + " minutes.");
        message.setFrom("noreply@bizrok.in");
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't fail the process
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
    }
    
    public enum OtpStatus {
        VALID, EXPIRED, MAX_ATTEMPTS_REACHED, NOT_FOUND, VERIFIED
    }
}