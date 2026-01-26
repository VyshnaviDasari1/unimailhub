package com.unimailhub.backend.repository;

import com.unimailhub.backend.entity.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MailRepository extends JpaRepository<Mail, Long> {

    List<Mail> findByToEmailAndTrashedFalseOrderByIdDesc(String toEmail);

    List<Mail> findByFromEmailAndTrashedFalseOrderByIdDesc(String fromEmail);

    List<Mail> findByToEmailAndStarredTrueAndTrashedFalseOrderByIdDesc(String email);

    List<Mail> findByTrashedTrueAndToEmailOrderByIdDesc(String email);

    // ✅ STARRED - emails sent OR received by user
    @Query("SELECT m FROM Mail m WHERE m.starred = true AND m.trashed = false AND (m.fromEmail = :email OR m.toEmail = :email) ORDER BY m.id DESC")
    List<Mail> findStarredMails(@Param("email") String email);

    // ✅ TRASH - emails sent OR received by user
    @Query("SELECT m FROM Mail m WHERE m.trashed = true AND (m.fromEmail = :email OR m.toEmail = :email) ORDER BY m.id DESC")
    List<Mail> findTrashedMails(@Param("email") String email);

         // 🔍 SEARCH – INBOX
    List<Mail> findByToEmailAndSubjectContainingIgnoreCaseAndTrashedFalseOrderByIdDesc(
            String email, String subject
    );

    // 🔍 SEARCH – SENT
    List<Mail> findByFromEmailAndSubjectContainingIgnoreCaseAndTrashedFalseOrderByIdDesc(
            String email, String subject
    );

    // 🔍 SEARCH – STARRED
    @Query("SELECT m FROM Mail m WHERE m.starred = true AND m.trashed = false AND (m.fromEmail = :email OR m.toEmail = :email) AND LOWER(m.subject) LIKE LOWER(CONCAT('%', :subject, '%')) ORDER BY m.id DESC")
    List<Mail> findStarredMailsBySubject(@Param("email") String email, @Param("subject") String subject);
}


