package com.bizrok.repository;

import com.bizrok.model.entity.PriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {
    
    List<PriceSnapshot> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    List<PriceSnapshot> findByOrderId(Long orderId);
}