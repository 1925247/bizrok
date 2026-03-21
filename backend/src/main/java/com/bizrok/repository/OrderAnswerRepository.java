package main.java.com.bizrok.repository;

import main.java.com.bizrok.model.entity.OrderAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OrderAnswer Repository
 * Provides database operations for OrderAnswer entity
 */
@Repository
public interface OrderAnswerRepository extends JpaRepository<OrderAnswer, Long> {
    
    List<OrderAnswer> findByOrder_Id(Long orderId);
    
    List<OrderAnswer> findByQuestion_Id(Long questionId);
    
    @Query("SELECT oa FROM OrderAnswer oa WHERE oa.order.id = :orderId AND oa.question.id = :questionId")
    Optional<OrderAnswer> findByOrderAndQuestion(@Param("orderId") Long orderId, @Param("questionId") Long questionId);
    
    @Query("SELECT oa FROM OrderAnswer oa LEFT JOIN FETCH oa.question LEFT JOIN FETCH oa.option WHERE oa.order.id = :orderId ORDER BY oa.question.sortOrder ASC")
    List<OrderAnswer> findByOrderWithQuestionAndOption(@Param("orderId") Long orderId);
}