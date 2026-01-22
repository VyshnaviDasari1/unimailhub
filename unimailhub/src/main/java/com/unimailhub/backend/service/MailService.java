package com.unimailhub.backend.service;

import com.unimailhub.backend.entity.Mail;
import com.unimailhub.backend.repository.MailRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MailService {

    private final MailRepository repo;

    public MailService(MailRepository repo) {
        this.repo = repo;
    }

    public void sendMail(Mail mail) {
        repo.save(mail);
    }

    public List<Mail> inbox(String email) {
        return repo.findByToEmailAndTrashedFalseOrderByCreatedAtDesc(email);
    }

    public List<Mail> sent(String email) {
        return repo.findByFromEmailAndTrashedFalseOrderByCreatedAtDesc(email);
    }

    public Mail getMail(Long id) {
    return repo.findById(id).orElse(null);
}

    public void toggleStar(Long id) {
        Mail mail = repo.findById(id).orElse(null);
        if (mail != null) {
            mail.setStarred(!mail.isStarred());
            repo.save(mail);
        }
    }

    public List<Mail> starred(String email) {
        return repo.findByStarredTrueAndTrashedFalseAndToEmailOrStarredTrueAndTrashedFalseAndFromEmailOrderByCreatedAtDesc(
                email, email
        );
    }

    public List<Mail> searchInbox(String email, String keyword) {
    return repo.findByToEmailAndSubjectContainingIgnoreCaseOrderByCreatedAtDesc(email, keyword);
    }

    public List<Mail> searchSent(String email, String keyword) {
        return repo.findByFromEmailAndSubjectContainingIgnoreCaseOrderByCreatedAtDesc(email, keyword);
    }

    public List<Mail> searchStarred(String email, String keyword) {
        return repo.findByToEmailAndStarredTrueAndSubjectContainingIgnoreCaseOrderByCreatedAtDesc(email, keyword);
    }

    public List<Mail> trash(String email) {
        return repo.findByTrashedTrueAndToEmailOrTrashedTrueAndFromEmailOrderByCreatedAtDesc(
                email, email
        );
    }

    public void moveToTrash(Long id) {
        Mail mail = repo.findById(id).orElse(null);
        if (mail != null) {
            mail.setTrashed(true);
            mail.setStarred(false); // optional: auto-remove star
            repo.save(mail);
        }
    }

}
