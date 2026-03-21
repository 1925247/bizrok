package main.java.com.bizrok.service;

import main.java.com.bizrok.model.dto.OrderRequest;
import main.java.com.bizrok.model.dto.OrderResponse;
import main.java.com.bizrok.model.entity.*;
import main.java.com.bizrok.repository.OrderRepository;
import main.java.com.bizrok.repository.OrderAnswerRepository;
import main.java.com.bizrok.repository.PriceSnapshotRepository;
import main.java.com.bizrok.repository.ModelRepository;
import main.java.com.bizrok.repository.UserRepository;
import main.java.com.bizrok.util.PriceCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Order Service for order management
 * Handles order creation, updates, and lifecycle management
 */
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderAnswerRepository orderAnswerRepository;
    
    @Autowired
    private PriceSnapshotRepository priceSnapshotRepository;
    
    @Autowired
    private ModelRepository modelRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PriceCalculator priceCalculator;
    
    @Autowired
    private SettingsService settingsService;
    
    /**
     * Create new order
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request, String userEmail) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get model
        Model model = modelRepository.findById(request.getModelId())
                .orElseThrow(() -> new RuntimeException("Model not found"));
        
        // Generate order number
        String orderNumber = generateOrderNumber();
        
        // Create order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .model(model)
                .status(Order.Status.CREATED)
                .pickupAddress(request.getPickupAddress())
                .pickupPincode(request.getPickupPincode())
                .pickupDate(request.getPickupDate())
                .pickupTime(request.getPickupTime())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankIfsc(request.getBankIfsc())
                .bankAccountName(request.getBankAccountName())
                .basePrice(model.getBasePrice())
                .finalPrice(model.getBasePrice()) // Will be updated after price calculation
                .totalDeductions(0.0)
                .kycVerified(false)
                .faceMatchVerified(false)
                .bankDetailsVerified(false)
                .build();
        
        order = orderRepository.save(order);
        
        // Save answers
        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            saveOrderAnswers(order, request.getAnswers());
        }
        
        // Calculate price
        List<OrderAnswer> answers = orderAnswerRepository.findByOrder_Id(order.getId());
        PriceCalculator.PriceCalculationResult priceResult = priceCalculator.calculatePrice(model.getBasePrice(), answers);
        
        if (priceResult.isSuccess()) {
            order.setFinalPrice(priceResult.getFinalPrice());
            order.setTotalDeductions(priceResult.getTotalDeductions());
            order = orderRepository.save(order);
            
            // Save price snapshot
            savePriceSnapshot(order, model, priceResult);
        }
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Get order by ID
     */
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithModelAndUser(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Get orders by user
     */
    public List<OrderResponse> getOrdersByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Order> orders = orderRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
        
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Update order status
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Order.Status newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(newStatus);
        order = orderRepository.save(order);
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Assign order to partner or field executive
     */
    @Transactional
    public OrderResponse assignOrder(Long orderId, Long assignedToUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        User assignedTo = userRepository.findById(assignedToUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is partner or field executive
        if (assignedTo.getRole() != User.Role.PARTNER && assignedTo.getRole() != User.Role.FIELD_EXECUTIVE) {
            throw new RuntimeException("User is not a partner or field executive");
        }
        
        order.setAssignedTo(assignedTo);
        order.setStatus(Order.Status.ASSIGNED);
        order = orderRepository.save(order);
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Update order details (for field executives)
     */
    @Transactional
    public OrderResponse updateOrderDetails(Long orderId, Double newFinalPrice, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setFinalPrice(newFinalPrice);
        order.setNotes(notes);
        order.setStatus(Order.Status.IN_PROGRESS);
        order = orderRepository.save(order);
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Complete order
     */
    @Transactional
    public OrderResponse completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(Order.Status.COMPLETED);
        order = orderRepository.save(order);
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Reject order
     */
    @Transactional
    public OrderResponse rejectOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(Order.Status.REJECTED);
        order.setNotes(reason);
        order = orderRepository.save(order);
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Update KYC status
     */
    @Transactional
    public OrderResponse updateKycStatus(Long orderId, boolean kycVerified) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setKycVerified(kycVerified);
        order = orderRepository.save(order);
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Update face match status
     */
    @Transactional
    public OrderResponse updateFaceMatchStatus(Long orderId, boolean faceMatchVerified) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setFaceMatchVerified(faceMatchVerified);
        order = orderRepository.save(order);
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Update bank details verification status
     */
    @Transactional
    public OrderResponse updateBankVerificationStatus(Long orderId, boolean bankVerified) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setBankDetailsVerified(bankVerified);
        order = orderRepository.save(order);
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Get orders by status
     */
    public List<OrderResponse> getOrdersByStatus(Order.Status status) {
        List<Order> orders = orderRepository.findByStatus(status);
        
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get unassigned orders
     */
    public List<OrderResponse> getUnassignedOrders() {
        List<Order> orders = orderRepository.findUnassignedOrdersByStatus(Order.Status.ASSIGNED);
        
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get orders assigned to user
     */
    public List<OrderResponse> getOrdersAssignedToUser(Long userId) {
        List<Order> orders = orderRepository.findByAssignedTo_Id(userId);
        
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }
    
    private void saveOrderAnswers(Order order, List<OrderRequest.AnswerRequest> answers) {
        for (OrderRequest.AnswerRequest answerRequest : answers) {
            OrderAnswer answer = OrderAnswer.builder()
                    .order(order)
                    .question(questionRepository.getReferenceById(answerRequest.getQuestionId()))
                    .option(answerRequest.getOptionId() != null ? 
                            optionRepository.getReferenceById(answerRequest.getOptionId()) : null)
                    .answerText(answerRequest.getAnswerText())
                    .imageUrl(answerRequest.getImageUrl())
                    .build();
            
            orderAnswerRepository.save(answer);
        }
    }
    
    private void savePriceSnapshot(Order order, Model model, PriceCalculator.PriceCalculationResult priceResult) {
        PriceSnapshot snapshot = PriceSnapshot.builder()
                .order(order)
                .model(model)
                .basePrice(model.getBasePrice())
                .totalDeductions(priceResult.getTotalDeductions())
                .finalPrice(priceResult.getFinalPrice())
                .groupDeductions("") // Could store JSON of group-wise deductions
                .build();
        
        priceSnapshotRepository.save(snapshot);
    }
    
    private String generateOrderNumber() {
        // Generate order number with timestamp
        return "ORD-" + System.currentTimeMillis();
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
        
        // Convert answers
        List<OrderAnswer> answers = orderAnswerRepository.findByOrder_Id(order.getId());
        List<OrderResponse.OrderAnswerDto> answerDtos = answers.stream()
                .map(answer -> {
                    OrderResponse.OrderAnswerDto dto = new OrderResponse.OrderAnswerDto();
                    dto.setId(answer.getId());
                    
                    // Convert question
                    if (answer.getQuestion() != null) {
                        OrderResponse.QuestionDto.QuestionDto questionDto = new OrderResponse.QuestionDto.QuestionDto();
                        questionDto.setId(answer.getQuestion().getId());
                        questionDto.setText(answer.getQuestion().getText());
                        questionDto.setSlug(answer.getQuestion().getSlug());
                        questionDto.setQuestionType(answer.getQuestion().getQuestionType().name());
                        dto.setQuestion(questionDto);
                    }
                    
                    // Convert option
                    if (answer.getOption() != null) {
                        OrderResponse.OptionDto optionDto = new OrderResponse.OptionDto();
                        optionDto.setId(answer.getOption().getId());
                        optionDto.setText(answer.getOption().getText());
                        optionDto.setSlug(answer.getOption().getSlug());
                        optionDto.setDeductionValue(answer.getOption().getDeductionValue());
                        optionDto.setDeductionType(answer.getOption().getDeductionType().name());
                        dto.setOption(optionDto);
                    }
                    
                    dto.setAnswerText(answer.getAnswerText());
                    dto.setImageUrl(answer.getImageUrl());
                    
                    return dto;
                })
                .collect(Collectors.toList());
        
        response.setAnswers(answerDtos);
        
        // Convert price snapshots
        List<PriceSnapshot> snapshots = priceSnapshotRepository.findByOrder_IdOrderByCalculatedAtDesc(order.getId());
        List<OrderResponse.PriceSnapshotDto> snapshotDtos = snapshots.stream()
                .map(snapshot -> {
                    OrderResponse.PriceSnapshotDto dto = new OrderResponse.PriceSnapshotDto();
                    dto.setId(snapshot.getId());
                    dto.setBasePrice(snapshot.getBasePrice());
                    dto.setGroupDeductions(snapshot.getGroupDeductions());
                    dto.setTotalDeductions(snapshot.getTotalDeductions());
                    dto.setFinalPrice(snapshot.getFinalPrice());
                    dto.setCalculatedAt(snapshot.getCalculatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
        
        response.setPriceSnapshots(snapshotDtos);
        
        return response;
    }
}