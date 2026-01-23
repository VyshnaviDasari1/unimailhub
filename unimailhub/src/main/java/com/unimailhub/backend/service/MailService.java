package com.unimailhub.backend.service;

import com.unimailhub.backend.entity.Attachment;
import com.unimailhub.backend.entity.Mail;
import com.unimailhub.backend.repository.AttachmentRepository;
import com.unimailhub.backend.repository.MailRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.io.File;

@Service
public class MailService {

    private final MailRepository mailRepository;
    private final AttachmentRepository attachmentRepository;

    public MailService(MailRepository mailRepository,
                   AttachmentRepository attachmentRepository) {
    this.mailRepository = mailRepository;
    this.attachmentRepository = attachmentRepository;

}


    // ✅ SEND MAIL
    public void sendMail(Mail mail, List<MultipartFile> files) {

    Mail savedMail = mailRepository.save(mail);

    if (files != null && !files.isEmpty()) {
        for (MultipartFile file : files) {

            if (file.isEmpty()) continue;

           String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filePath = uploadDir + System.currentTimeMillis()
                    + "_" + file.getOriginalFilename();


            try {
                file.transferTo(new File(filePath));

                Attachment attachment = new Attachment();
                attachment.setFileName(file.getOriginalFilename());
                attachment.setFileType(file.getContentType());
                attachment.setFilePath(filePath);
                attachment.setMail(savedMail);

                attachmentRepository.save(attachment);

         } catch (IOException e) {
            e.printStackTrace(); // <-- IMPORTANT
            throw new RuntimeException("File upload failed", e);
        }

        }
    }
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
