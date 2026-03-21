package com.bizrok.repository;

import com.bizrok.model.entity.SubGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubGroupRepository extends JpaRepository<SubGroup, Long> {
    
    List<SubGroup> findByGroupId(Long groupId);
    
    List<SubGroup> findByGroupIdAndIsActiveTrue(Long groupId);
    
    Optional<SubGroup> findByNameAndGroupId(String name, Long groupId);
    
    boolean existsByNameAndGroupId(String name, Long groupId);
}