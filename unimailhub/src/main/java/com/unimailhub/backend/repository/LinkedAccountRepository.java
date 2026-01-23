package com.unimailhub.backend.repository;

import com.unimailhub.backend.entity.LinkedAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinkedAccountRepository extends JpaRepository<LinkedAccount, Long> {
    List<LinkedAccount> findByOwnerEmail(String ownerEmail);
}
