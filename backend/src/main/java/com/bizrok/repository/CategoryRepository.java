package com.bizrok.repository;

import com.bizrok.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByBrandId(Long brandId);
    
    Optional<Category> findByNameAndBrandId(String name, Long brandId);
    
    boolean existsByNameAndBrandId(String name, Long brandId);
}