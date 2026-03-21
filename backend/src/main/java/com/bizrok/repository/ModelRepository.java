package main.java.com.bizrok.repository;

import main.java.com.bizrok.model.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Model Repository
 * Provides database operations for Model entity
 */
@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    
    Optional<Model> findBySlug(String slug);
    
    Boolean existsBySlug(String slug);
    
    List<Model> findByBrand_IdAndIsActiveTrueOrderBySortOrderAsc(Long brandId);
    
    @Query("SELECT m FROM Model m WHERE m.brand.id = :brandId AND m.isActive = true ORDER BY m.sortOrder ASC, m.name ASC")
    List<Model> findActiveModelsByBrand(@Param("brandId") Long brandId);
    
    @Query("SELECT DISTINCT m FROM Model m JOIN FETCH m.brand b JOIN FETCH b.category WHERE m.isActive = true")
    List<Model> findActiveModelsWithBrandsAndCategories();
    
    @Query("SELECT m FROM Model m WHERE m.brand.category.id = :categoryId AND m.isActive = true ORDER BY m.brand.name ASC, m.name ASC")
    List<Model> findActiveModelsWithCategory(@Param("categoryId") Long categoryId);
}