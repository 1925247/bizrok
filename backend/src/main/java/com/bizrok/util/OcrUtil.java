package com.bizrok.util;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class OcrUtil {
    
    private final ITesseract tesseract;
    
    public OcrUtil() {
        tesseract = new Tesseract();
        // Set the path to tessdata directory
        tesseract.setDatapath("src/main/resources/tessdata");
        // Set language to English
        tesseract.setLanguage("eng");
        // Set page segmentation mode
        tesseract.setPageSegMode(1); // Automatic page segmentation with OSD
        // Set OCR engine mode
        tesseract.setOcrEngineMode(1); // Neural nets LSTM engine only
    }
    
    /**
     * Extract text from an image file
     */
    public String extractText(MultipartFile imageFile) throws IOException, TesseractException {
        // Convert MultipartFile to File
        File tempFile = convertMultiPartToFile(imageFile);
        
        try {
            // Perform OCR
            String result = tesseract.doOCR(tempFile);
            return result.trim();
        } finally {
            // Clean up temp file
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    /**
     * Extract specific fields from document
     */
    public Map<String, String> extractDocumentFields(MultipartFile imageFile) throws IOException, TesseractException {
        String fullText = extractText(imageFile);
        
        Map<String, String> fields = new HashMap<>();
        
        // Extract name (common patterns for Indian documents)
        fields.put("name", extractName(fullText));
        
        // Extract document number (Aadhaar, PAN, Driving License patterns)
        fields.put("documentNumber", extractDocumentNumber(fullText));
        
        // Extract date of birth
        fields.put("dateOfBirth", extractDateOfBirth(fullText));
        
        // Extract address (simplified extraction)
        fields.put("address", extractAddress(fullText));
        
        return fields;
    }
    
    /**
     * Extract name from OCR text
     */
    private String extractName(String text) {
        // Common patterns for names in Indian documents
        String[] namePatterns = {
            "Name", "Name:", "NAME", "NAME:", 
            "Father's Name", "Father's Name:", "FATHER'S NAME",
            "Mother's Name", "Mother's Name:", "MOTHER'S NAME",
            "Husband's Name", "Husband's Name:", "HUSBAND'S NAME"
        };
        
        for (String pattern : namePatterns) {
            int index = text.indexOf(pattern);
            if (index != -1) {
                // Extract text after the pattern
                String remainingText = text.substring(index + pattern.length()).trim();
                // Find the first newline or end of line
                int endIndex = remainingText.indexOf('\n');
                if (endIndex != -1) {
                    String name = remainingText.substring(0, endIndex).trim();
                    if (!name.isEmpty() && name.length() > 2) {
                        return name;
                    }
                }
            }
        }
        
        return "";
    }
    
    /**
     * Extract document number (Aadhaar, PAN, Driving License)
     */
    private String extractDocumentNumber(String text) {
        // Aadhaar pattern: 12 digits
        java.util.regex.Pattern aadhaarPattern = java.util.regex.Pattern.compile("\\b\\d{4}\\s?\\d{4}\\s?\\d{4}\\b");
        java.util.regex.Matcher aadhaarMatcher = aadhaarPattern.matcher(text);
        if (aadhaarMatcher.find()) {
            return aadhaarMatcher.group().replaceAll("\\s", "");
        }
        
        // PAN pattern: 5 letters + 4 digits + 1 letter
        java.util.regex.Pattern panPattern = java.util.regex.Pattern.compile("\\b[A-Z]{5}\\d{4}[A-Z]\\b");
        java.util.regex.Matcher panMatcher = panPattern.matcher(text);
        if (panMatcher.find()) {
            return panMatcher.group();
        }
        
        // Driving License pattern: State code + year + number
        java.util.regex.Pattern dlPattern = java.util.regex.Pattern.compile("\\b[A-Z]{2}\\d{13}\\b");
        java.util.regex.Matcher dlMatcher = dlPattern.matcher(text);
        if (dlMatcher.find()) {
            return dlMatcher.group();
        }
        
        return "";
    }
    
    /**
     * Extract date of birth
     */
    private String extractDateOfBirth(String text) {
        // Common date patterns
        java.util.regex.Pattern datePattern = java.util.regex.Pattern.compile(
            "\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{2,4}[/-]\\d{1,2}[/-]\\d{1,2})\\b"
        );
        java.util.regex.Matcher dateMatcher = datePattern.matcher(text);
        
        while (dateMatcher.find()) {
            String dateStr = dateMatcher.group();
            // Basic validation - check if it looks like a reasonable date
            if (isValidDate(dateStr)) {
                return dateStr;
            }
        }
        
        return "";
    }
    
    /**
     * Extract address (simplified)
     */
    private String extractAddress(String text) {
        // Look for address patterns
        String[] addressKeywords = {
            "Address", "Address:", "ADDRESS", "ADDRESS:",
            "Residence", "Residence:", "RESIDENCE"
        };
        
        for (String keyword : addressKeywords) {
            int index = text.indexOf(keyword);
            if (index != -1) {
                String remainingText = text.substring(index + keyword.length()).trim();
                // Extract multiple lines for address
                String[] lines = remainingText.split("\n");
                StringBuilder address = new StringBuilder();
                
                for (int i = 0; i < Math.min(lines.length, 5); i++) {
                    String line = lines[i].trim();
                    if (!line.isEmpty() && line.length() > 3) {
                        if (address.length() > 0) {
                            address.append(", ");
                        }
                        address.append(line);
                    }
                }
                
                if (address.length() > 10) {
                    return address.toString();
                }
            }
        }
        
        return "";
    }
    
    /**
     * Basic date validation
     */
    private boolean isValidDate(String dateStr) {
        try {
            // Simple validation - check format and reasonable ranges
            String[] parts = dateStr.split("[/-]");
            if (parts.length == 3) {
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                
                return day >= 1 && day <= 31 && 
                       month >= 1 && month <= 12 && 
                       year >= 1900 && year <= 2026;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }
    
    /**
     * Convert MultipartFile to File
     */
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;
    }
}