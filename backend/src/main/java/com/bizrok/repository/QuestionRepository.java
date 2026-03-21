package com.bizrok.repository;

import com.bizrok.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    List<Question> findByIsActiveTrueOrderBySortOrderAsc();
    
    List<Question> findByGroupId(Long groupId);
    
    List<Question> findBySubGroupId(Long subGroupId);
    
    List<Question> findByGroupIdAndIsActiveTrue(Long groupId);
    
    List<Question> findBySubGroupIdAndIsActiveTrue(Long subGroupId);
    
    Optional<Question> findBySlug(String slug);
}