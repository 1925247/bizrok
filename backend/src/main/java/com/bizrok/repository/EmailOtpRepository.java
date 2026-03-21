package main.java.com.bizrok.repository;

import main.java.com.bizrok.model.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Email OTP Repository
 * Provides database operations for EmailOtp entity
 */
@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    
    Optional<EmailOtp> findByEmail(String email);
    
    @Query("SELECT e FROM EmailOtp e WHERE e.email = :email AND e.isVerified = false AND e.expiryTime > :currentTime")
    Optional<EmailOtp> findActiveOtpByEmail(@Param("email") String email, @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT e FROM EmailOtp e WHERE e.email = :email AND e.otp = :otp AND e.isVerified = false AND e.expiryTime > :currentTime")
    Optional<EmailOtp> findByEmailAndOtp(@Param("email") String email, @Param("otp") String otp, @Param("currentTime") LocalDateTime currentTime);
    
    @Query("DELETE FROM EmailOtp e WHERE e.expiryTime < :currentTime OR e.isVerified = true")
    void deleteExpiredOrVerifiedOtps(@Param("currentTime") LocalDateTime currentTime);
    
    List<EmailOtp> findByEmailAndCreatedAtAfter(String email, LocalDateTime timeLimit);
}