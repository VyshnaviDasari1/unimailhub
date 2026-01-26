package com.unimailhub.backend.repository;

import com.unimailhub.backend.entity.SecurityToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityTokenRepository extends JpaRepository<SecurityToken, Long> {
    SecurityToken findByToken(String token);
}