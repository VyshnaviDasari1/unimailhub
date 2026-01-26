package com.unimailhub.backend.controller;

import com.unimailhub.backend.entity.User;
import com.unimailhub.backend.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.unimailhub.backend.entity.LinkedAccount;
import com.unimailhub.backend.entity.Mail;
import com.unimailhub.backend.service.MailService;
import com.unimailhub.backend.service.SettingsService;
import com.unimailhub.backend.service.SecurityService;
import com.unimailhub.backend.service.EmailService;
import com.unimailhub.backend.repository.UserRepository;
import com.unimailhub.backend.repository.AttachmentRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;



@Controller
public class AuthController {

    private final UserService userService;
    private final MailService mailService;
     private final SettingsService settingsService;
     private final SecurityService securityService;
     private final EmailService emailService;
     public final UserRepository userRepository;
     private final AttachmentRepository attachmentRepository;


    public AuthController(UserService userService,
         MailService mailService, SettingsService settingsService, UserRepository userRepository,
         SecurityService securityService, EmailService emailService, AttachmentRepository attachmentRepository) {
        this.userService = userService;
        this.mailService = mailService;
        this.settingsService = settingsService;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.emailService = emailService;
        this.attachmentRepository = attachmentRepository;
    }

    /* ===================== AUTH ===================== */

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(User user, HttpSession session, Model model, HttpServletRequest request) {
        String result = userService.login(user);

        if (!"success".equals(result)) {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }

        String ip = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        User existingUser = userService.findByEmail(user.getEmail());

        if (existingUser.getLastKnownIP() == null || existingUser.getLastKnownUserAgent() == null) {
            // First login, set as known device
            existingUser.setLastKnownIP(ip);
            existingUser.setLastKnownUserAgent(userAgent);
            userService.updateUser(existingUser);
            session.setAttribute("email", user.getEmail());
            return "redirect:/home";
        }

        if (securityService.isKnownDevice(existingUser, ip, userAgent) ||
            securityService.hasApprovedLoginAttempt(user.getEmail(), ip, userAgent)) {
            // Known device or previously approved
            session.setAttribute("email", user.getEmail());
            return "redirect:/home";
        }

        // New device, create pending attempt and send email
        var attempt = securityService.createPendingLoginAttempt(user.getEmail(), ip, userAgent);
        securityService.sendSecurityEmail(attempt);

        model.addAttribute("message", "A security email has been sent to your email address. Please check your email and approve the login.");
        return "login-pending";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(User user, Model model) {
        String result = userService.register(user);

        if (!"success".equals(result)) {
            model.addAttribute("error", result);
            return "register";
        }

        emailService.sendWelcomeEmail(user.getEmail());

        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    /* ===================== HOME (CENTER PANEL LOGIC) ===================== */

    @GetMapping("/home")
    public String home(
            @RequestParam(defaultValue = "inbox") String tab,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long read,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/login";

        boolean hasSearch = (search != null && !search.trim().isEmpty());

        if ("trash".equals(tab)) {
            model.addAttribute("mails", mailService.trash(email));
        } else if ("starred".equals(tab)) {
            model.addAttribute("mails",
                    hasSearch
                            ? mailService.searchStarred(email, search)
                            : mailService.starred(email));
        } else if ("sent".equals(tab)) {
            model.addAttribute("mails",
                    hasSearch
                            ? mailService.searchSent(email, search)
                            : mailService.sent(email));
        } else {
            model.addAttribute("mails",
                    hasSearch
                            ? mailService.searchInbox(email, search)
                            : mailService.inbox(email));
        }

        // 🔹 Read mail (popup)
        if (read != null) {
            Mail selectedMail = mailService.getMail(read);
            model.addAttribute("selectedMail", selectedMail);
        }

        model.addAttribute("activeTab", tab);
        model.addAttribute("linkedAccounts",
            settingsService.getLinkedAccounts(email));
        model.addAttribute("search", search);
        model.addAttribute("userEmail", email);

        return "home";
    }


    /* ===================== MAIL ACTIONS ===================== */

    @PostMapping("/send")
    public String sendMail(
            Mail mail,
            @RequestParam("files") List<MultipartFile> files,
            HttpSession session) {

        String fromEmail = (String) session.getAttribute("email");
        mail.setFromEmail(fromEmail);

        mailService.sendMail(mail, files);

        return "redirect:/home";
    }


    @GetMapping("/mail/{id}")
    public String readMail(@PathVariable Long id, Model model) {
        Mail mail = mailService.getMail(id);
        model.addAttribute("mail", mail);
        return "read-mail";
    }

    @GetMapping("/star/{id}")
    public String toggleStar(@PathVariable Long id,
                             @RequestParam(defaultValue = "inbox") String tab) {

        mailService.toggleStar(id);
        return "redirect:/home?tab=" + tab;
    }

    @GetMapping("/trash/{id}")
    public String moveToTrash(@PathVariable Long id,
                            @RequestParam(defaultValue = "inbox") String tab) {

        mailService.moveToTrash(id);
        return "redirect:/home?tab=" + tab;
    }

    @GetMapping("/delete/{id}")
    public String deleteMailPermanently(@PathVariable Long id) {
        mailService.deletePermanently(id);
        return "redirect:/home?tab=trash";
    }
    @PostMapping("/settings/save")
public String saveSettings(@RequestParam String linkedEmail,
                           @RequestParam String password,
                           HttpSession session) {

    String primaryEmail = (String) session.getAttribute("email");
    if (primaryEmail == null) {
        return "redirect:/login";
    }

    settingsService.addLinkedAccount(primaryEmail, linkedEmail, password);

    // ✅ stay on settings page so user sees added email
    return "redirect:/home?tab=settings";
}
@GetMapping("/forgot-password")
public String forgotPasswordPage() {
    return "forgot-password";
}

@PostMapping("/forgot-password")
public String handleForgotPassword(@RequestParam String email) {

    System.out.println("Password reset requested for: " + email);
    System.out.println("Reset link: http://localhost:8080/reset-password?email=" + email);

    return "redirect:/login";
}
@GetMapping("/reset-password")
public String resetPasswordPage(
        @RequestParam String email,
        Model model) {

    model.addAttribute("email", email);
    return "reset-password";
}

@PostMapping("/reset-password")
public String handleResetPassword(
        @RequestParam String email,
        @RequestParam String newPassword) {

    User user = userRepository.findByEmail(email);

    if (user != null) {
        user.setPassword(newPassword);
        userRepository.save(user);
        System.out.println("Password updated for: " + email);
    }

    return "redirect:/login";
}

    /* ===================== SECURITY ENDPOINTS ===================== */

    @GetMapping("/security/approve")
    public String approveLogin(@RequestParam String token, HttpSession session, Model model) {
        String result = securityService.processSecurityToken(token, "approve");
        if ("approved".equals(result)) {
            // Find the approved attempt and log the user in
            var tokenEntity = securityService.getTokenByToken(token); // Need to add this method
            if (tokenEntity != null) {
                session.setAttribute("email", tokenEntity.getEmail());
                return "redirect:/home";
            }
        }
        model.addAttribute("error", "Link expired or already used");
        return "login-denied";
    }

    @GetMapping("/security/deny")
    public String denyLogin(@RequestParam String token, Model model) {
        String result = securityService.processSecurityToken(token, "deny");
        if ("denied".equals(result)) {
            model.addAttribute("message", "Login attempt has been denied.");
        } else {
            model.addAttribute("error", "Link expired or already used");
        }
        return "login-denied";
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /* ===================== ATTACHMENT DOWNLOAD ===================== */

    @GetMapping("/attachment/download/{id}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            return ResponseEntity.status(403).build();
        }

        // Find the attachment
        var attachmentOpt = attachmentRepository.findById(id);
        if (attachmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var attachment = attachmentOpt.get();

        // Verify the user has access to this attachment (through the mail)
        var mail = attachment.getMail();
        if (!email.equalsIgnoreCase(mail.getToEmail()) && !email.equalsIgnoreCase(mail.getFromEmail())) {
            return ResponseEntity.status(403).build();
        }

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + attachment.getFileName() + "\"")
                        .header(HttpHeaders.CONTENT_TYPE, attachment.getFileType() != null ?
                                attachment.getFileType() : "application/octet-stream")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

}
