package com.unimailhub.backend.repository;

import com.unimailhub.backend.entity.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MailRepository extends JpaRepository<Mail, Long> {

    List<Mail> findByToEmailOrderByCreatedAtDesc(String toEmail);

    List<Mail> findByFromEmailOrderByCreatedAtDesc(String fromEmail);

    List<Mail> findByToEmailAndStarredTrueOrderByCreatedAtDesc(String toEmail);

    // Inbox search
    List<Mail> findByToEmailAndSubjectContainingIgnoreCaseOrderByCreatedAtDesc(
            String toEmail, String subject);

    // Sent search
    List<Mail> findByFromEmailAndSubjectContainingIgnoreCaseOrderByCreatedAtDesc(
            String fromEmail, String subject);

    // Starred search
    List<Mail> findByToEmailAndStarredTrueAndSubjectContainingIgnoreCaseOrderByCreatedAtDesc(
            String toEmail, String subject);

    List<Mail> findByStarredTrueAndToEmailOrStarredTrueAndFromEmailOrderByCreatedAtDesc(
        String toEmail, String fromEmail);

    // Inbox (not trashed)
    List<Mail> findByToEmailAndTrashedFalseOrderByCreatedAtDesc(String toEmail);

    // Sent (not trashed)
    List<Mail> findByFromEmailAndTrashedFalseOrderByCreatedAtDesc(String fromEmail);

    // Starred (not trashed)
    List<Mail> findByStarredTrueAndTrashedFalseAndToEmailOrStarredTrueAndTrashedFalseAndFromEmailOrderByCreatedAtDesc(
            String toEmail, String fromEmail);

    // Trash
    List<Mail> findByTrashedTrueAndToEmailOrTrashedTrueAndFromEmailOrderByCreatedAtDesc(
            String toEmail, String fromEmail);

}
