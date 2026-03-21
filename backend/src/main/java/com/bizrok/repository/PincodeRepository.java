package main.java.com.bizrok.repository;

import main.java.com.bizrok.model.entity.Pincode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Pincode Repository
 * Provides database operations for Pincode entity
 */
@Repository
public interface PincodeRepository extends JpaRepository<Pincode, Long> {
    
    Optional<Pincode> findByPincode(String pincode);
    
    Boolean existsByPincode(String pincode);
    
    List<Pincode> findByIsActiveTrue();
    
    List<Pincode> findByPartner_Id(Long partnerId);
    
    @Query("SELECT p FROM Pincode p WHERE p.isActive = true AND p.pincode LIKE :prefix%")
    List<Pincode> findActiveByPincodePrefix(@Param("prefix") String prefix);
    
    @Query("SELECT p FROM Pincode p WHERE p.isActive = true AND p.city = :city")
    List<Pincode> findActiveByCity(@Param("city") String city);
    
    @Query("SELECT p FROM Pincode p LEFT JOIN FETCH p.partner WHERE p.pincode = :pincode")
    Optional<Pincode> findByPincodeWithPartner(@Param("pincode") String pincode);
}