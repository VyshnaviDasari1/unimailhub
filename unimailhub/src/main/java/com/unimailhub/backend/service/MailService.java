package com.unimailhub.backend.service;

import com.unimailhub.backend.entity.Attachment;
import com.unimailhub.backend.entity.Mail;
import com.unimailhub.backend.repository.AttachmentRepository;
import com.unimailhub.backend.repository.MailRepository;
import com.unimailhub.backend.service.JobService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.io.File;

@Service
public class MailService {

    private final MailRepository mailRepository;
    private final AttachmentRepository attachmentRepository;
    private final JobService jobService;

    public MailService(MailRepository mailRepository,
                   AttachmentRepository attachmentRepository,
                   JobService jobService) {
    this.mailRepository = mailRepository;
    this.attachmentRepository = attachmentRepository;
    this.jobService = jobService;

}


    // ✅ SEND MAIL
    public void sendMail(Mail mail, List<MultipartFile> files) {

    // Save mail for primary recipient (toEmail)
    Mail savedMail = mailRepository.save(mail);

    // Process job opportunities from the email
    jobService.processEmailForJob(savedMail);

    // Handle attachments for primary mail
    if (files != null && !files.isEmpty()) {
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

           String uploadDir = System.getProperty("user.dir") + "/../uploads/";
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
            e.printStackTrace();
            throw new RuntimeException("File upload failed", e);
        }
        }
    }

    // Handle CC recipients - create separate mail records for each CC
    if (mail.getCc() != null && !mail.getCc().trim().isEmpty()) {
        String[] ccEmails = mail.getCc().split(",");
        for (String ccEmail : ccEmails) {
            ccEmail = ccEmail.trim();
            if (!ccEmail.isEmpty()) {
                // Create new mail record for CC recipient
                Mail ccMail = new Mail();
                ccMail.setFromEmail(mail.getFromEmail());
                ccMail.setToEmail(ccEmail);
                ccMail.setCc(mail.getCc()); // Keep original CC list
                ccMail.setSubject(mail.getSubject());
                ccMail.setMessage(mail.getMessage());
                ccMail.setStarred(false);
                ccMail.setTrashed(false);

                Mail savedCcMail = mailRepository.save(ccMail);

                // Process job opportunities for CC recipient
                jobService.processEmailForJob(savedCcMail);

                // Copy attachments to CC mail
                if (files != null && !files.isEmpty()) {
                    for (MultipartFile file : files) {
                        if (file.isEmpty()) continue;

                        String uploadDir = System.getProperty("user.dir") + "/../uploads/";
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
                            attachment.setMail(savedCcMail);

                            attachmentRepository.save(attachment);

                     } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("File upload failed", e);
                    }
                    }
                }
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
        return mailRepository.findStarredMails(email.toLowerCase());
    }

    // ✅ TRASH
    public List<Mail> trash(String email) {
        return mailRepository.findTrashedMails(email.toLowerCase());
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
        return mailRepository.findStarredMailsBySubject(email.toLowerCase(), keyword);
    }

}
