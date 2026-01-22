package com.unimailhub.backend.repository;

import com.unimailhub.backend.entity.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MailRepository extends JpaRepository<Mail, Long> {

    List<Mail> findByToEmailOrderByCreatedAtDesc(String toEmail);

    List<Mail> findByFromEmailOrderByCreatedAtDesc(String fromEmail);

    List<Mail> findByToEmailAndStarredTrueOrderByCreatedAtDesc(String toEmail);

}
