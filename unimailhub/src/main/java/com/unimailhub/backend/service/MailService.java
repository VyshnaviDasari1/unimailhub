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
        return repo.findByToEmailOrderByCreatedAtDesc(email);
    }

    public List<Mail> sent(String email) {
        return repo.findByFromEmailOrderByCreatedAtDesc(email);
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
        return repo.findByToEmailAndStarredTrueOrderByCreatedAtDesc(email);
    }

}
