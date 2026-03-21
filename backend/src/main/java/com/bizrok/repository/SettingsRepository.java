package main.java.com.bizrok.repository;

import main.java.com.bizrok.model.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Settings Repository
 * Provides database operations for Settings entity
 * Core component of config-driven architecture
 */
@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
    
    Optional<Settings> findByKey(String key);
    
    Boolean existsByKey(String key);
    
    List<Settings> findByIsActiveTrue();
    
    @Query("SELECT s FROM Settings s WHERE s.isActive = true AND s.key IN :keys")
    List<Settings> findActiveSettingsByKeys(@Param("keys") List<String> keys);
    
    @Query("SELECT s FROM Settings s WHERE s.isActive = true ORDER BY s.key ASC")
    List<Settings> findAllActiveSettingsSorted();
}