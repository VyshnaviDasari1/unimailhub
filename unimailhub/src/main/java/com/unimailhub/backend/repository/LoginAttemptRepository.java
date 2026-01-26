package com.unimailhub.backend.repository;

import com.unimailhub.backend.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByEmailAndStatus(String email, String status);
    LoginAttempt findByIdAndStatus(Long id, String status);
}