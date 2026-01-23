package com.unimailhub.backend.repository;

import com.unimailhub.backend.entity.Mail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MailRepository extends JpaRepository<Mail, Long> {

    List<Mail> findByToEmailAndTrashedFalseOrderByIdDesc(String toEmail);

    List<Mail> findByFromEmailAndTrashedFalseOrderByIdDesc(String fromEmail);

    List<Mail> findByToEmailAndStarredTrueAndTrashedFalseOrderByIdDesc(String email);

    List<Mail> findByTrashedTrueAndToEmailOrderByIdDesc(String email);

         // 🔍 SEARCH – INBOX
    List<Mail> findByToEmailAndSubjectContainingIgnoreCaseAndTrashedFalseOrderByIdDesc(
            String email, String subject
    );

    // 🔍 SEARCH – SENT
    List<Mail> findByFromEmailAndSubjectContainingIgnoreCaseAndTrashedFalseOrderByIdDesc(
            String email, String subject
    );

    // 🔍 SEARCH – STARRED
    List<Mail> findByToEmailAndStarredTrueAndSubjectContainingIgnoreCaseAndTrashedFalseOrderByIdDesc(
            String email, String subject
    );
}


