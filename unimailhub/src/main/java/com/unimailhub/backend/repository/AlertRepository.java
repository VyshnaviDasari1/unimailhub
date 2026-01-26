package com.unimailhub.backend.repository;

import com.unimailhub.backend.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<Alert> findByUserEmailAndIsReadFalseOrderByCreatedAtDesc(String userEmail);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.id = :id AND a.userEmail = :userEmail")
    void markAsRead(@Param("id") Long id, @Param("userEmail") String userEmail);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.userEmail = :userEmail AND a.isRead = false")
    void markAllAsRead(@Param("userEmail") String userEmail);
}