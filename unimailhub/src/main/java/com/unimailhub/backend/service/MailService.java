package com.unimailhub.backend.service;

import com.unimailhub.backend.entity.Mail;
import com.unimailhub.backend.repository.MailRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailService {

    private final MailRepository mailRepository;

    // ✅ Constructor injection (MANDATORY)
    public MailService(MailRepository mailRepository) {
        this.mailRepository = mailRepository;
    }

    // ✅ SEND MAIL
    public void sendMail(Mail mail) {

        mail.setStarred(false);
        mail.setTrashed(false);

        // Normalize emails (VERY IMPORTANT)
        mail.setFromEmail(mail.getFromEmail().toLowerCase());
        mail.setToEmail(mail.getToEmail().toLowerCase());

        mailRepository.save(mail);
    }

    // ✅ INBOX
    public List<Mail> inbox(String email) {
        return mailRepository
                .findByToEmailAndTrashedFalseOrderByIdDesc(
                        email.toLowerCase()
                );
    }

    // ✅ SENT
    public List<Mail> sent(String email) {
        return mailRepository
                .findByFromEmailAndTrashedFalseOrderByIdDesc(
                        email.toLowerCase()
                );
    }

    // ✅ STARRED
    public List<Mail> starred(String email) {
        return mailRepository
                .findByToEmailAndStarredTrueAndTrashedFalseOrderByIdDesc(
                        email.toLowerCase()
                );
    }

    // ✅ TRASH
    public List<Mail> trash(String email) {
        return mailRepository
                .findByTrashedTrueAndToEmailOrderByIdDesc(
                        email.toLowerCase()
                );
    }

    // ✅ READ MAIL
    public Mail getMail(Long id) {
        return mailRepository.findById(id).orElse(null);
    }

    // ✅ TOGGLE STAR
    public void toggleStar(Long id) {
        Mail mail = mailRepository.findById(id).orElse(null);
        if (mail != null) {
            mail.setStarred(!mail.isStarred());
            mailRepository.save(mail);
        }
    }

    // ✅ SOFT DELETE (MOVE TO TRASH)
    public void moveToTrash(Long id) {
        Mail mail = mailRepository.findById(id).orElse(null);
        if (mail != null) {
            mail.setTrashed(true);
            mailRepository.save(mail);
        }
    }

    // ✅ PERMANENT DELETE
    public void deletePermanently(Long id) {
        mailRepository.deleteById(id);
    }

    // 🔍 SEARCH – INBOX
    public List<Mail> searchInbox(String email, String keyword) {
        return mailRepository
                .findByToEmailAndSubjectContainingIgnoreCaseAndTrashedFalseOrderByIdDesc(
                        email.toLowerCase(),
                        keyword
                );
    }

    // 🔍 SEARCH – SENT
    public List<Mail> searchSent(String email, String keyword) {
        return mailRepository
                .findByFromEmailAndSubjectContainingIgnoreCaseAndTrashedFalseOrderByIdDesc(
                        email.toLowerCase(),
                        keyword
                );
    }

    // 🔍 SEARCH – STARRED
    public List<Mail> searchStarred(String email, String keyword) {
        return mailRepository
                .findByToEmailAndStarredTrueAndSubjectContainingIgnoreCaseAndTrashedFalseOrderByIdDesc(
                        email.toLowerCase(),
                        keyword
                );
    }

}
