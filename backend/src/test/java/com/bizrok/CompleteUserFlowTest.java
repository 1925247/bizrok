package com.bizrok;

import com.bizrok.model.entity.*;
import com.bizrok.repository.*;
import com.bizrok.service.*;
import com.bizrok.util.OcrUtil;
import com.bizrok.util.FaceDetectionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CompleteUserFlowTest {

    @Autowired
    private BrandRepository brandRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ModelRepository modelRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private SubGroupRepository subGroupRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private OptionRepository optionRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private OcrUtil ocrUtil;
    
    @Autowired
    private FaceDetectionUtil faceDetectionUtil;

    @Test
    public void testCompleteUserFlow() throws Exception {
        // Step 1: Setup test data
        setupTestData();
        
        // Step 2: Test device selection and model retrieval
        List<Model> models = modelRepository.findByCategoryId(1L);
        assertFalse(models.isEmpty(), "Should have models available");
        
        Model selectedModel = models.get(0);
        
        // Step 3: Test question retrieval
        List<Question> questions = questionService.getQuestions();
        assertFalse(questions.isEmpty(), "Should have questions for assessment");
        
        // Step 4: Test price calculation
        List<OrderAnswer> answers = createTestAnswers(questions);
        Map<String, Object> priceResult = orderService.calculatePrice(selectedModel.getId(), answers);
        
        assertNotNull(priceResult, "Price calculation should return result");
        assertTrue(priceResult.containsKey("finalPrice"), "Should contain final price");
        assertTrue(priceResult.containsKey("totalDeductions"), "Should contain total deductions");
        
        // Step 5: Test order creation
        Order order = createTestOrder(selectedModel, answers, priceResult);
        assertNotNull(order, "Order should be created");
        assertNotNull(order.getId(), "Order should have ID");
        assertEquals("CREATED", order.getStatus(), "Order should be in CREATED status");
        
        // Step 6: Test KYC document processing (mock)
        // In real test, you would use actual image files
        // For now, we'll test the utility methods exist and are callable
        
        // Test OCR utility (would need actual image file in real test)
        // String extractedText = ocrUtil.extractText(mockDocumentImage);
        // assertNotNull(extractedText, "OCR should extract text");
        
        // Test face detection utility (would need actual image file in real test)
        // Map<String, Object> faceResult = faceDetectionUtil.detectFaces(mockSelfieImage);
        // assertNotNull(faceResult, "Face detection should return result");
        
        // Step 7: Test order tracking
        Order retrievedOrder = orderService.getOrder(order.getId());
        assertNotNull(retrievedOrder, "Order should be retrievable");
        assertEquals(order.getId(), retrievedOrder.getId(), "Order ID should match");
        
        // Step 8: Test order status updates
        orderService.updateOrderStatus(order.getId(), "ASSIGNED");
        Order updatedOrder = orderService.getOrder(order.getId());
        assertEquals("ASSIGNED", updatedOrder.getStatus(), "Order status should be updated");
        
        // Step 9: Test price snapshots
        List<PriceSnapshot> priceSnapshots = orderService.getPriceHistory(order.getId());
        assertFalse(priceSnapshots.isEmpty(), "Should have price snapshots");
        
        // Step 10: Test order completion
        orderService.updateOrderStatus(order.getId(), "COMPLETED");
        Order completedOrder = orderService.getOrder(order.getId());
        assertEquals("COMPLETED", completedOrder.getStatus(), "Order should be completed");
    }
    
    @Test
    public void testQuestionValidation() {
        List<Question> questions = questionService.getQuestions();
        
        // Test that questions have proper structure
        for (Question question : questions) {
            assertNotNull(question.getText(), "Question should have text");
            assertNotNull(question.getQuestionType(), "Question should have type");
            assertTrue(question.getIsActive(), "Question should be active");
            
            // Test options for non-text questions
            if (!"text".equals(question.getQuestionType())) {
                assertFalse(question.getOptions().isEmpty(), "Non-text questions should have options");
                
                for (Option option : question.getOptions()) {
                    assertNotNull(option.getText(), "Option should have text");
                    assertTrue(option.getIsActive(), "Option should be active");
                }
            }
        }
    }
    
    @Test
    public void testPriceCalculationLogic() {
        // Test that price calculation works correctly
        setupTestData();
        
        Model model = modelRepository.findById(1L).orElse(null);
        assertNotNull(model, "Test model should exist");
        
        List<Question> questions = questionService.getQuestions();
        List<OrderAnswer> answers = createTestAnswers(questions);
        
        Map<String, Object> priceResult = orderService.calculatePrice(model.getId(), answers);
        
        // Verify price structure
        assertTrue(priceResult.containsKey("basePrice"), "Should have base price");
        assertTrue(priceResult.containsKey("finalPrice"), "Should have final price");
        assertTrue(priceResult.containsKey("totalDeductions"), "Should have total deductions");
        assertTrue(priceResult.containsKey("breakdown"), "Should have breakdown");
        
        // Verify that final price is less than or equal to base price
        Double basePrice = (Double) priceResult.get("basePrice");
        Double finalPrice = (Double) priceResult.get("finalPrice");
        Double totalDeductions = (Double) priceResult.get("totalDeductions");
        
        assertTrue(finalPrice <= basePrice, "Final price should not exceed base price");
        assertEquals(basePrice - totalDeductions, finalPrice, 0.01, "Final price should equal base price minus deductions");
    }
    
    private void setupTestData() {
        // Create Brand
        Brand brand = new Brand();
        brand.setName("Test Brand");
        brand.setDescription("Test brand for user flow testing");
        brand.setActive(true);
        brand = brandRepository.save(brand);
        
        // Create Category
        Category category = new Category();
        category.setName("Smartphones");
        category.setDescription("Mobile phones");
        category.setActive(true);
        category.setBrand(brand);
        category = categoryRepository.save(category);
        
        // Create Model
        Model model = new Model();
        model.setName("Test Phone");
        model.setBrandName("Test Brand");
        model.setCategoryName("Smartphones");
        model.setBasePrice(10000.0);
        model.setVariantInfo("6GB RAM, 128GB Storage");
        model.setActive(true);
        model.setCategory(category);
        model = modelRepository.save(model);
        
        // Create Group
        Group group = new Group();
        group.setName("Physical Condition");
        group.setDescription("Questions about physical condition");
        group.setActive(true);
        group = groupRepository.save(group);
        
        // Create SubGroup
        SubGroup subGroup = new SubGroup();
        subGroup.setName("Screen Condition");
        subGroup.setDescription("Questions about screen condition");
        subGroup.setActive(true);
        subGroup.setGroup(group);
        subGroup = subGroupRepository.save(subGroup);
        
        // Create Question
        Question question = new Question();
        question.setText("Is the screen cracked?");
        question.setSlug("screen-cracked");
        question.setQuestionType("radio");
        question.setRequired(true);
        question.setActive(true);
        question.setGroup(group);
        question.setSubGroup(subGroup);
        question = questionRepository.save(question);
        
        // Create Options
        Option optionYes = new Option();
        optionYes.setText("Yes");
        optionYes.setSlug("yes");
        optionYes.setDeductionValue(2000.0);
        optionYes.setDeductionType("FIXED");
        optionYes.setActive(true);
        optionYes.setQuestion(question);
        optionRepository.save(optionYes);
        
        Option optionNo = new Option();
        optionNo.setText("No");
        optionNo.setSlug("no");
        optionNo.setDeductionValue(0.0);
        optionNo.setDeductionType("FIXED");
        optionNo.setActive(true);
        optionNo.setQuestion(question);
        optionRepository.save(optionNo);
    }
    
    private List<OrderAnswer> createTestAnswers(List<Question> questions) {
        // Create test answers for the questions
        // This would typically come from user input
        return List.of(); // Simplified for testing
    }
    
    private Order createTestOrder(Model model, List<OrderAnswer> answers, Map<String, Object> priceResult) {
        Order order = new Order();
        order.setOrderNumber("TEST-" + System.currentTimeMillis());
        order.setStatus("CREATED");
        order.setBasePrice((Double) priceResult.get("basePrice"));
        order.setFinalPrice((Double) priceResult.get("finalPrice"));
        order.setTotalDeductions((Double) priceResult.get("totalDeductions"));
        order.setPickupAddress("Test Address");
        order.setPickupPincode("110001");
        order.setPickupDate("2024-01-01");
        order.setPickupTime("10:00 AM - 12:00 PM");
        order.setBankAccountNumber("1234567890");
        order.setBankIfsc("SBIN0001234");
        order.setBankAccountName("Test User");
        order.setKycVerified(false);
        order.setFaceMatchVerified(false);
        order.setBankDetailsVerified(false);
        order.setModel(model);
        
        return orderRepository.save(order);
    }
}