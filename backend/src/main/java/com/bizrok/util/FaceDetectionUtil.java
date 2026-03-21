package com.bizrok.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Face Detection Utility for KYC verification
 * Mock implementation for demo purposes - OpenCV not available
 */
@Component
public class FaceDetectionUtil {
    
    private final Random random = new Random();
    
    public FaceDetectionUtil() {
        System.out.println("FaceDetectionUtil initialized with mock implementation");
    }
    
    /**
     * Detect faces in an image (Mock implementation)
     */
    public Map<String, Object> detectFaces(MultipartFile imageFile) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        // Mock implementation - simulate face detection
        int faceCount = random.nextInt(2) + 1; // 1-2 faces
        boolean facesDetected = faceCount > 0;
        
        result.put("faceCount", faceCount);
        result.put("facesDetected", facesDetected);
        result.put("mock", true);
        result.put("message", "Mock face detection - actual OpenCV not available");
        
        if (facesDetected) {
            Map<String, Object> faceDetails = new HashMap<>();
            faceDetails.put("x", 100 + random.nextInt(50));
            faceDetails.put("y", 100 + random.nextInt(50));
            faceDetails.put("width", 150 + random.nextInt(50));
            faceDetails.put("height", 150 + random.nextInt(50));
            result.put("largestFace", faceDetails);
            
            Map<String, Object> quality = new HashMap<>();
            quality.put("valid", true);
            quality.put("faceSizeRatio", 0.15 + random.nextDouble() * 0.1);
            quality.put("faceSizeValid", true);
            quality.put("centered", true);
            quality.put("centerRatio", 0.75 + random.nextDouble() * 0.2);
            quality.put("eyesDetected", true);
            quality.put("eyeCount", 2);
            result.put("faceQuality", quality);
        }
        
        return result;
    }
    
    /**
     * Compare two faces for similarity (Mock implementation)
     */
    public Map<String, Object> compareFaces(MultipartFile documentImage, MultipartFile selfieImage) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        // Mock implementation - simulate face comparison
        double similarity = 0.65 + random.nextDouble() * 0.3; // 65-95% similarity
        boolean match = similarity > 0.6;
        
        result.put("match", match);
        result.put("confidence", similarity);
        result.put("docFaceDetected", true);
        result.put("selfieFaceDetected", true);
        result.put("mock", true);
        result.put("message", "Mock face comparison - actual OpenCV not available");
        
        return result;
    }
    
    /**
     * Assess face quality for KYC purposes (Mock implementation)
     */
    public Map<String, Object> assessFaceQuality(MultipartFile imageFile) throws IOException {
        Map<String, Object> quality = new HashMap<>();
        
        // Mock implementation - simulate quality assessment
        quality.put("valid", true);
        quality.put("faceSizeRatio", 0.12 + random.nextDouble() * 0.08);
        quality.put("faceSizeValid", true);
        quality.put("centered", true);
        quality.put("centerRatio", 0.7 + random.nextDouble() * 0.25);
        quality.put("eyesDetected", true);
        quality.put("eyeCount", 2);
        quality.put("mock", true);
        quality.put("message", "Mock face quality assessment - actual OpenCV not available");
        
        return quality;
    }
}