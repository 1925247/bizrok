package main.java.com.bizrok.repository;

import main.java.com.bizrok.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Repository
 * Provides database operations for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    Boolean existsByOrderNumber(String orderNumber);
    
    List<Order> findByUser_IdOrderByCreatedAtDesc(Long userId);
    
    List<Order> findByStatus(Order.Status status);
    
    List<Order> findByAssignedTo_Id(Long assignedToId);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findByUserAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<Order.Status> statuses);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.assignedTo IS NULL")
    List<Order> findUnassignedOrdersByStatus(@Param("status") Order.Status status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt < :endDate")
    Long countOrdersInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.model LEFT JOIN FETCH o.user WHERE o.id = :orderId")
    Optional<Order> findByIdWithModelAndUser(@Param("orderId") Long orderId);
}