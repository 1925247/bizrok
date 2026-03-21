package main.java.com.bizrok.service;

import main.java.com.bizrok.model.entity.*;
import main.java.com.bizrok.repository.*;
import main.java.com.bizrok.model.dto.ModelDto;
import main.java.com.bizrok.model.dto.QuestionDto;
import main.java.com.bizrok.model.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin Service for admin panel functionality
 * Provides admin-level operations for managing the platform
 */
@Service
public class AdminService {
    
    @Autowired
    private SettingsRepository settingsRepository;
    
    @Autowired
    private ModelRepository modelRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private OptionRepository optionRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private KycDocumentRepository kycDocumentRepository;
    
    @Autowired
    private PincodeRepository pincodeRepository;
    
    /**
     * Update system settings
     */
    @Transactional
    public Settings updateSetting(String key, String value, boolean isActive) {
        Settings setting = settingsRepository.findByKey(key)
                .orElse(Settings.builder().key(key).build());
        
        setting.setValue(value);
        setting.setActive(isActive);
        setting.setUpdatedAt(LocalDateTime.now());
        
        return settingsRepository.save(setting);
    }
    
    /**
     * Get all system settings
     */
    public List<Settings> getAllSettings() {
        return settingsRepository.findAll();
    }
    
    /**
     * Toggle feature enable/disable
     */
    @Transactional
    public Settings toggleFeature(String featureKey, boolean enable) {
        return updateSetting(featureKey, enable ? "true" : "false", enable);
    }
    
    /**
     * Create or update model
     */
    @Transactional
    public ModelDto createOrUpdateModel(ModelDto modelDto) {
        Model model = modelRepository.findById(modelDto.getId())
                .orElse(Model.builder().build());
        
        model.setName(modelDto.getName());
        model.setSlug(modelDto.getSlug());
        model.setBrandName(modelDto.getBrandName());
        model.setCategoryName(modelDto.getCategoryName());
        model.setBasePrice(modelDto.getBasePrice());
        model.setVariantInfo(modelDto.getVariantInfo());
        model.setImageUrl(modelDto.getImageUrl());
        model.setActive(modelDto.getIsActive());
        model.setSortOrder(modelDto.getSortOrder());
        model.setUpdatedAt(LocalDateTime.now());
        
        if (model.getId() == null) {
            model.setCreatedAt(LocalDateTime.now());
        }
        
        model = modelRepository.save(model);
        
        return convertToModelDto(model);
    }
    
    /**
     * Get all models
     */
    public List<ModelDto> getAllModels() {
        return modelRepository.findAll().stream()
                .map(this::convertToModelDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Create or update question
     */
    @Transactional
    public QuestionDto createOrUpdateQuestion(QuestionDto questionDto) {
        Question question = questionRepository.findById(questionDto.getId())
                .orElse(Question.builder().build());
        
        // Set question properties
        question.setText(questionDto.getText());
        question.setSlug(questionDto.getSlug());
        question.setQuestionType(Question.QuestionType.valueOf(questionDto.getQuestionType()));
        question.setIsRequired(questionDto.getRequired());
        question.setActive(questionDto.getActive());
        question.setSortOrder(questionDto.getSortOrder());
        question.setUpdatedAt(LocalDateTime.now());
        
        if (question.getId() == null) {
            question.setCreatedAt(LocalDateTime.now());
        }
        
        question = questionRepository.save(question);
        
        return convertToQuestionDto(question);
    }
    
    /**
     * Create or update option
     */
    @Transactional
    public QuestionDto.OptionDto createOrUpdateOption(QuestionDto.OptionDto optionDto) {
        Option option = optionRepository.findById(optionDto.getId())
                .orElse(Option.builder().build());
        
        option.setText(optionDto.getText());
        option.setSlug(optionDto.getSlug());
        option.setDeductionValue(optionDto.getDeductionValue());
        option.setDeductionType(Option.DeductionType.valueOf(optionDto.getDeductionType()));
        option.setImageUrl(optionDto.getImageUrl());
        option.setActive(optionDto.getActive());
        option.setSortOrder(optionDto.getSortOrder());
        option.setUpdatedAt(LocalDateTime.now());
        
        if (option.getId() == null) {
            option.setCreatedAt(LocalDateTime.now());
        }
        
        option = optionRepository.save(option);
        
        return convertToOptionDto(option);
    }
    
    /**
     * Get all questions with options
     */
    public List<QuestionDto> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(this::convertToQuestionDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get dashboard statistics
     */
    public AdminDashboardStats getDashboardStats() {
        AdminDashboardStats stats = new AdminDashboardStats();
        
        // User statistics
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByIsActiveTrue());
        stats.setKycVerifiedUsers(userRepository.countByKycVerifiedTrue());
        
        // Order statistics
        stats.setTotalOrders(orderRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus(Order.Status.CREATED));
        stats.setCompletedOrders(orderRepository.countByStatus(Order.Status.COMPLETED));
        stats.setRejectedOrders(orderRepository.countByStatus(Order.Status.REJECTED));
        
        // Revenue statistics (sum of final prices of completed orders)
        stats.setTotalRevenue(orderRepository.sumFinalPricesByStatus(Order.Status.COMPLETED));
        
        // KYC statistics
        stats.setPendingKycDocuments(kycDocumentRepository.countByStatus(KycDocument.Status.PENDING_MANUAL));
        
        // Model statistics
        stats.setTotalModels(modelRepository.count());
        stats.setActiveModels(modelRepository.countByIsActiveTrue());
        
        return stats;
    }
    
    /**
     * Get orders by status
     */
    public List<OrderResponse> getOrdersByStatus(Order.Status status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get users by role
     */
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Update user role
     */
    @Transactional
    public User updateUserRole(Long userId, User.Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Activate/deactivate user
     */
    @Transactional
    public User toggleUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setActive(active);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Get KYC documents for review
     */
    public List<KycDocument> getKycDocumentsForReview() {
        return kycDocumentRepository.findByStatus(KycDocument.Status.PENDING_MANUAL);
    }
    
    /**
     * Manage service pincodes
     */
    @Transactional
    public Pincode addServiceablePincode(String pincode, String city, Long partnerId) {
        Pincode existingPincode = pincodeRepository.findByPincode(pincode);
        if (existingPincode != null) {
            throw new RuntimeException("Pincode already exists");
        }
        
        Pincode newPincode = Pincode.builder()
                .pincode(pincode)
                .city(city)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        if (partnerId != null) {
            User partner = userRepository.findById(partnerId)
                    .orElseThrow(() -> new RuntimeException("Partner not found"));
            newPincode.setPartner(partner);
        }
        
        return pincodeRepository.save(newPincode);
    }
    
    /**
     * Get all serviceable pincodes
     */
    public List<Pincode> getAllPincodes() {
        return pincodeRepository.findAll();
    }
    
    /**
     * Toggle pincode service availability
     */
    @Transactional
    public Pincode togglePincode(String pincode, boolean active) {
        Pincode existingPincode = pincodeRepository.findByPincode(pincode);
        if (existingPincode == null) {
            throw new RuntimeException("Pincode not found");
        }
        
        existingPincode.setActive(active);
        existingPincode.setUpdatedAt(LocalDateTime.now());
        
        return pincodeRepository.save(existingPincode);
    }
    
    private ModelDto convertToModelDto(Model model) {
        ModelDto dto = new ModelDto();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setSlug(model.getSlug());
        dto.setBrandName(model.getBrandName());
        dto.setCategoryName(model.getCategoryName());
        dto.setBasePrice(model.getBasePrice());
        dto.setVariantInfo(model.getVariantInfo());
        dto.setImageUrl(model.getImageUrl());
        dto.setActive(model.getIsActive());
        dto.setSortOrder(model.getSortOrder());
        return dto;
    }
    
    private QuestionDto convertToQuestionDto(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setText(question.getText());
        dto.setSlug(question.getSlug());
        dto.setQuestionType(question.getQuestionType().name());
        dto.setRequired(question.getIsRequired());
        dto.setActive(question.getIsActive());
        dto.setSortOrder(question.getSortOrder());
        
        // Convert options
        List<Option> options = optionRepository.findActiveOptionsByQuestion(question.getId());
        List<QuestionDto.OptionDto> optionDtos = options.stream()
                .map(this::convertToOptionDto)
                .collect(Collectors.toList());
        
        dto.setOptions(optionDtos);
        
        return dto;
    }
    
    private QuestionDto.OptionDto convertToOptionDto(Option option) {
        QuestionDto.OptionDto dto = new QuestionDto.OptionDto();
        dto.setId(option.getId());
        dto.setText(option.getText());
        dto.setSlug(option.getSlug());
        dto.setDeductionValue(option.getDeductionValue());
        dto.setDeductionType(option.getDeductionType().name());
        dto.setImageUrl(option.getImageUrl());
        dto.setActive(option.getIsActive());
        dto.setSortOrder(option.getSortOrder());
        return dto;
    }
    
    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus().name());
        response.setBasePrice(order.getBasePrice());
        response.setFinalPrice(order.getFinalPrice());
        response.setTotalDeductions(order.getTotalDeductions());
        response.setPickupAddress(order.getPickupAddress());
        response.setPickupPincode(order.getPickupPincode());
        response.setPickupDate(order.getPickupDate());
        response.setPickupTime(order.getPickupTime());
        response.setBankAccountNumber(order.getBankAccountNumber());
        response.setBankIfsc(order.getBankIfsc());
        response.setBankAccountName(order.getBankAccountName());
        response.setKycVerified(order.getKycVerified());
        response.setFaceMatchVerified(order.getFaceMatchVerified());
        response.setBankDetailsVerified(order.getBankDetailsVerified());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        
        // Convert model
        if (order.getModel() != null) {
            OrderResponse.ModelDto modelDto = new OrderResponse.ModelDto();
            modelDto.setId(order.getModel().getId());
            modelDto.setName(order.getModel().getName());
            modelDto.setSlug(order.getModel().getSlug());
            modelDto.setBrandName(order.getModel().getBrandName());
            modelDto.setCategoryName(order.getModel().getCategoryName());
            modelDto.setBasePrice(order.getModel().getBasePrice());
            modelDto.setVariantInfo(order.getModel().getVariantInfo());
            modelDto.setImageUrl(order.getModel().getImageUrl());
            modelDto.setActive(order.getModel().getIsActive());
            modelDto.setSortOrder(order.getModel().getSortOrder());
            response.setModel(modelDto);
        }
        
        return response;
    }
    
    /**
     * Admin Dashboard Statistics
     */
    public static class AdminDashboardStats {
        private long totalUsers;
        private long activeUsers;
        private long kycVerifiedUsers;
        private long totalOrders;
        private long pendingOrders;
        private long completedOrders;
        private long rejectedOrders;
        private double totalRevenue;
        private long pendingKycDocuments;
        private long totalModels;
        private long activeModels;
        
        // Getters and setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
        public long getKycVerifiedUsers() { return kycVerifiedUsers; }
        public void setKycVerifiedUsers(long kycVerifiedUsers) { this.kycVerifiedUsers = kycVerifiedUsers; }
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        public long getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
        public long getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }
        public long getRejectedOrders() { return rejectedOrders; }
        public void setRejectedOrders(long rejectedOrders) { this.rejectedOrders = rejectedOrders; }
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        public long getPendingKycDocuments() { return pendingKycDocuments; }
        public void setPendingKycDocuments(long pendingKycDocuments) { this.pendingKycDocuments = pendingKycDocuments; }
        public long getTotalModels() { return totalModels; }
        public void setTotalModels(long totalModels) { this.totalModels = totalModels; }
        public long getActiveModels() { return activeModels; }
        public void setActiveModels(long activeModels) { this.activeModels = activeModels; }
    }
}