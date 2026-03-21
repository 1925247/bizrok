package com.bizrok.repository;

import com.bizrok.model.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    
    List<Option> findByQuestionId(Long questionId);
    
    List<Option> findByQuestionIdAndIsActiveTrue(Long questionId);
    
    Optional<Option> findByQuestionIdAndId(Long questionId, Long id);
}