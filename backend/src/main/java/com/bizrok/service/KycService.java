package main.java.com.bizrok.service;

import main.java.com.bizrok.model.entity.User;
import main.java.com.bizrok.model.entity.KycDocument;
import main.java.com.bizrok.repository.UserRepository;
import main.java.com.bizrok.repository.KycDocumentRepository;
import main.java.com.bizrok.util.OcrUtil;
import main.java.com.bizrok.util.FaceDetectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * KYC Service for document verification and biometric validation
 */
@Service
public class KycService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private KycDocumentRepository kycDocumentRepository;
    
    @Autowired
    private OcrUtil ocrUtil;
    
    @Autowired
    private FaceDetectionUtil faceDetectionUtil;
    
    @Autowired
    private SettingsService settingsService;
    
    /**
     * Submit KYC documents for verification
     */
    @Transactional
    public KycSubmissionResult submitKycDocuments(String userEmail, 
                                                  MultipartFile documentImage, 
                                                  MultipartFile selfieImage) throws IOException {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if KYC is enabled
        if (!settingsService.isKycEnabled()) {
            return KycSubmissionResult.error("KYC verification is currently disabled");
        }
        
        // Validate document quality
        OcrUtil.DocumentQuality documentQuality = ocrUtil.assessDocumentQuality(documentImage);
        if (!"GOOD".equals(documentQuality.getQualityLevel())) {
            return KycSubmissionResult.error("Document quality is poor: " + documentQuality.getIssues());
        }
        
        // Validate selfie quality
        FaceDetectionUtil.FaceQuality selfieQuality = faceDetectionUtil.assessFaceQuality(selfieImage);
        if (!"GOOD".equals(selfieQuality.getQualityLevel())) {
            return KycSubmissionResult.error("Selfie quality is poor: " + selfieQuality.getIssues());
        }
        
        // Extract document data
        Map<String, String> documentData = extractDocumentData(documentImage);
        if (documentData.isEmpty()) {
            return KycSubmissionResult.error("Could not extract data from document");
        }
        
        // Save KYC document
        KycDocument kycDocument = KycDocument.builder()
                .user(user)
                .documentType(KycDocument.DocumentType.AADHAAR) // Default, could be determined from OCR
                .documentNumber(documentData.get("aadhaarNumber"))
                .name(documentData.get("name"))
                .dob(documentData.get("dob"))
                .gender(documentData.get("gender"))
                .documentImage(documentImage.getBytes())
                .selfieImage(selfieImage.getBytes())
                .status(KycDocument.Status.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();
        
        kycDocument = kycDocumentRepository.save(kycDocument);
        
        // Perform face match verification
        FaceDetectionUtil.FaceMatchResult faceMatchResult = faceDetectionUtil.compareFaces(documentImage, selfieImage);
        
        if (faceMatchResult.isSuccess() && faceMatchResult.isMatch()) {
            // Auto-verify if face match is successful
            kycDocument.setStatus(KycDocument.Status.VERIFIED);
            kycDocument.setVerifiedAt(LocalDateTime.now());
            kycDocument.setVerificationNotes("Auto-verified via face match (confidence: " + faceMatchResult.getConfidence() + ")");
            kycDocument = kycDocumentRepository.save(kycDocument);
            
            // Update user KYC status
            user.setKycVerified(true);
            userRepository.save(user);
            
            return KycSubmissionResult.success("KYC verified successfully", kycDocument.getId());
        } else {
            // Manual verification required
            kycDocument.setStatus(KycDocument.Status.PENDING_MANUAL);
            kycDocument.setVerificationNotes("Face match failed, manual verification required");
            kycDocument = kycDocumentRepository.save(kycDocument);
            
            return KycSubmissionResult.pending("KYC submitted for manual verification", kycDocument.getId());
        }
    }
    
    /**
     * Verify KYC document manually
     */
    @Transactional
    public KycVerificationResult verifyKycDocument(Long documentId, boolean isVerified, String notes) {
        KycDocument kycDocument = kycDocumentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("KYC document not found"));
        
        if (kycDocument.getStatus() != KycDocument.Status.PENDING_MANUAL) {
            return KycVerificationResult.error("Document is not pending manual verification");
        }
        
        kycDocument.setStatus(isVerified ? KycDocument.Status.VERIFIED : KycDocument.Status.REJECTED);
        kycDocument.setVerifiedAt(LocalDateTime.now());
        kycDocument.setVerificationNotes(notes);
        
        kycDocument = kycDocumentRepository.save(kycDocument);
        
        // Update user KYC status
        if (isVerified) {
            User user = kycDocument.getUser();
            user.setKycVerified(true);
            userRepository.save(user);
        }
        
        return KycVerificationResult.success("KYC document " + (isVerified ? "verified" : "rejected"));
    }
    
    /**
     * Get user KYC status
     */
    public KycStatus getKycStatus(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<KycDocument> documents = kycDocumentRepository.findByUser_IdOrderBySubmittedAtDesc(user.getId());
        
        KycDocument latestDocument = documents.isEmpty() ? null : documents.get(0);
        
        return KycStatus.builder()
                .isKycVerified(user.getKycVerified())
                .latestDocumentStatus(latestDocument != null ? latestDocument.getStatus().name() : null)
                .verificationNotes(latestDocument != null ? latestDocument.getVerificationNotes() : null)
                .submittedAt(latestDocument != null ? latestDocument.getSubmittedAt() : null)
                .verifiedAt(latestDocument != null ? latestDocument.getVerifiedAt() : null)
                .build();
    }
    
    /**
     * Get pending KYC documents for admin review
     */
    public List<KycDocument> getPendingKycDocuments() {
        return kycDocumentRepository.findByStatus(KycDocument.Status.PENDING_MANUAL);
    }
    
    /**
     * Extract document data based on document type
     */
    private Map<String, String> extractDocumentData(MultipartFile documentImage) throws IOException {
        try {
            // Try to extract Aadhaar data first
            Map<String, String> aadhaarData = ocrUtil.extractAadhaarData(documentImage);
            if (!aadhaarData.isEmpty() && aadhaarData.containsKey("aadhaarNumber")) {
                return aadhaarData;
            }
            
            // Try to extract PAN data
            Map<String, String> panData = ocrUtil.extractPanData(documentImage);
            if (!panData.isEmpty() && panData.containsKey("panNumber")) {
                return panData;
            }
            
            // Try to extract driving license data
            Map<String, String> licenseData = ocrUtil.extractDrivingLicenseData(documentImage);
            if (!licenseData.isEmpty() && licenseData.containsKey("licenseNumber")) {
                return licenseData;
            }
            
            return Map.of(); // Empty if no data extracted
            
        } catch (Exception e) {
            // Log error but continue
            System.err.println("OCR extraction failed: " + e.getMessage());
            return Map.of();
        }
    }
    
    /**
     * KYC Submission Result
     */
    public static class KycSubmissionResult {
        private final boolean success;
        private final boolean isPending;
        private final String message;
        private final Long documentId;
        private final String errorMessage;
        
        private KycSubmissionResult(boolean success, boolean isPending, String message, Long documentId, String errorMessage) {
            this.success = success;
            this.isPending = isPending;
            this.message = message;
            this.documentId = documentId;
            this.errorMessage = errorMessage;
        }
        
        public static KycSubmissionResult success(String message, Long documentId) {
            return new KycSubmissionResult(true, false, message, documentId, null);
        }
        
        public static KycSubmissionResult pending(String message, Long documentId) {
            return new KycSubmissionResult(true, true, message, documentId, null);
        }
        
        public static KycSubmissionResult error(String errorMessage) {
            return new KycSubmissionResult(false, false, null, null, errorMessage);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public boolean isPending() { return isPending; }
        public String getMessage() { return message; }
        public Long getDocumentId() { return documentId; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * KYC Verification Result
     */
    public static class KycVerificationResult {
        private final boolean success;
        private final String message;
        private final String errorMessage;
        
        private KycVerificationResult(boolean success, String message, String errorMessage) {
            this.success = success;
            this.message = message;
            this.errorMessage = errorMessage;
        }
        
        public static KycVerificationResult success(String message) {
            return new KycVerificationResult(true, message, null);
        }
        
        public static KycVerificationResult error(String errorMessage) {
            return new KycVerificationResult(false, null, errorMessage);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * KYC Status
     */
    public static class KycStatus {
        private boolean isKycVerified;
        private String latestDocumentStatus;
        private String verificationNotes;
        private LocalDateTime submittedAt;
        private LocalDateTime verifiedAt;
        
        // Getters and setters
        public boolean isKycVerified() { return isKycVerified; }
        public void setKycVerified(boolean kycVerified) { isKycVerified = kycVerified; }
        public String getLatestDocumentStatus() { return latestDocumentStatus; }
        public void setLatestDocumentStatus(String latestDocumentStatus) { this.latestDocumentStatus = latestDocumentStatus; }
        public String getVerificationNotes() { return verificationNotes; }
        public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }
        public LocalDateTime getSubmittedAt() { return submittedAt; }
        public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
        public LocalDateTime getVerifiedAt() { return verifiedAt; }
        public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    }
}