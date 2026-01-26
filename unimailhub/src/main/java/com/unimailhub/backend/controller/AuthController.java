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
import com.unimailhub.backend.service.AlertService;
import com.unimailhub.backend.service.AccountService;
import com.unimailhub.backend.service.JobService;

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
     private final AccountService accountService;
     private final JobService jobService;
     private final AlertService alertService;


    public AuthController(UserService userService,
         MailService mailService, SettingsService settingsService, UserRepository userRepository,
         SecurityService securityService, EmailService emailService, AttachmentRepository attachmentRepository,
         AccountService accountService, JobService jobService, AlertService alertService) {
        this.userService = userService;
        this.mailService = mailService;
        this.settingsService = settingsService;
        this.userRepository = userRepository;
        this.securityService = securityService;
        this.emailService = emailService;
        this.attachmentRepository = attachmentRepository;
        this.accountService = accountService;
        this.jobService = jobService;
        this.alertService = alertService;
    }

    /* ===================== AUTH ===================== */

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(User user, HttpSession session, Model model, HttpServletRequest request) {
        User authenticatedUser = accountService.authenticate(user.getEmail(), user.getPassword());

        if (authenticatedUser == null) {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }

        String ip = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        // Check if this is a new device login
        if (authenticatedUser.getLastKnownIP() == null || authenticatedUser.getLastKnownUserAgent() == null) {
            // First login, set as known device
            authenticatedUser.setLastKnownIP(ip);
            authenticatedUser.setLastKnownUserAgent(userAgent);
            userService.updateUser(authenticatedUser);
        } else if (!securityService.isKnownDevice(authenticatedUser, ip, userAgent) &&
                   !securityService.hasApprovedLoginAttempt(user.getEmail(), ip, userAgent)) {
            // New device, create pending attempt and send email
            var attempt = securityService.createPendingLoginAttempt(user.getEmail(), ip, userAgent);
            securityService.sendSecurityEmail(attempt);

            model.addAttribute("message", "A security email has been sent to your email address. Please check your email and approve the login.");
            return "login-pending";
        }

        // Add account to session
        AccountService.SessionAccounts sessionAccounts = getSessionAccounts(session);
        sessionAccounts.addAccount(user.getEmail());

        // Set as active account
        setActiveEmail(session, user.getEmail());

        return "redirect:/home";
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
        AccountService.SessionAccounts sessionAccounts = getSessionAccounts(session);
        String activeEmail = getActiveEmail(session);

        if (activeEmail != null) {
            sessionAccounts.removeAccount(activeEmail);
        }

        // If no accounts left, invalidate session
        if (sessionAccounts.getAccounts().isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        // Otherwise, switch to first available account
        setActiveEmail(session, sessionAccounts.getActiveAccount());
        return "redirect:/home";
    }

    /* ===================== HOME (CENTER PANEL LOGIC) ===================== */

    @GetMapping("/home")
    public String home(
            @RequestParam(defaultValue = "inbox") String tab,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Long read,
            HttpSession session,
            Model model) {

        String email = getActiveEmail(session);
        if (email == null) return "redirect:/login";

        boolean hasSearch = (search != null && !search.trim().isEmpty());
        boolean hasJobSearch = (keyword != null && !keyword.trim().isEmpty()) || (jobType != null && !jobType.trim().isEmpty());

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
        } else if ("jobs".equals(tab)) {
            model.addAttribute("jobs",
                    hasJobSearch
                            ? jobService.searchJobs(email, keyword, jobType, null)
                            : jobService.getUserJobs(email));
            model.addAttribute("keyword", keyword);
            model.addAttribute("jobType", jobType);
        } else if ("alerts".equals(tab)) {
            model.addAttribute("alerts", alertService.getAlerts(email));
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
        model.addAttribute("search", search);
        model.addAttribute("userEmail", email);

        // Add session accounts for profile menu
        AccountService.SessionAccounts sessionAccounts = getSessionAccounts(session);
        model.addAttribute("sessionAccounts", sessionAccounts.getAccounts());
        model.addAttribute("activeAccount", email);

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


    /* ===================== ALERT ACTIONS ===================== */

    @PostMapping("/alerts/mark-read/{id}")
    public String markAlertAsRead(@PathVariable Long id, HttpSession session) {
        String email = getActiveEmail(session);
        if (email != null) {
            alertService.markAsRead(id, email);
        }
        return "redirect:/home?tab=alerts";
    }

    @PostMapping("/alerts/mark-all-read")
    public String markAllAlertsAsRead(HttpSession session) {
        String email = getActiveEmail(session);
        if (email != null) {
            alertService.markAllAsRead(email);
        }
        return "redirect:/home?tab=alerts";
    }

    @GetMapping("/delete/{id}")
    public String deleteMailPermanently(@PathVariable Long id) {
        mailService.deletePermanently(id);
        return "redirect:/home?tab=trash";
    }

    @GetMapping("/jobs/apply/{id}")
    public String markJobApplied(@PathVariable Long id, HttpSession session) {
        String email = getActiveEmail(session);
        if (email != null) {
            jobService.applyToJob(id, email);
        }
        return "redirect:/home?tab=jobs";
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

    private AccountService.SessionAccounts getSessionAccounts(HttpSession session) {
        AccountService.SessionAccounts accounts = (AccountService.SessionAccounts) session.getAttribute("sessionAccounts");
        if (accounts == null) {
            accounts = new AccountService.SessionAccounts();
            session.setAttribute("sessionAccounts", accounts);
        }
        return accounts;
    }

    private String getActiveEmail(HttpSession session) {
        AccountService.SessionAccounts accounts = getSessionAccounts(session);
        return accounts.getActiveAccount();
    }

    private void setActiveEmail(HttpSession session, String email) {
        AccountService.SessionAccounts accounts = getSessionAccounts(session);
        accounts.setActiveAccount(email);
        session.setAttribute("email", email); // Keep backward compatibility
    }

    /* ===================== ACCOUNT MANAGEMENT ===================== */

    @GetMapping("/add-account")
    public String showAddAccountForm() {
        return "add-account";
    }

    @PostMapping("/add-account")
    public String addAccount(User user, HttpSession session, Model model) {
        User authenticatedUser = accountService.authenticate(user.getEmail(), user.getPassword());

        if (authenticatedUser == null) {
            model.addAttribute("error", "Invalid credentials");
            return "add-account";
        }

        // Add account to session
        AccountService.SessionAccounts sessionAccounts = getSessionAccounts(session);
        sessionAccounts.addAccount(user.getEmail());

        // Set as active account
        setActiveEmail(session, user.getEmail());

        return "redirect:/home";
    }

    @GetMapping("/switch-account")
    public String switchAccount(@RequestParam String email, HttpSession session) {
        AccountService.SessionAccounts sessionAccounts = getSessionAccounts(session);

        if (sessionAccounts.hasAccount(email)) {
            setActiveEmail(session, email);
        }

        return "redirect:/home";
    }

    @GetMapping("/logout-account")
    public String logoutAccount(@RequestParam String email, HttpSession session) {
        AccountService.SessionAccounts sessionAccounts = getSessionAccounts(session);
        sessionAccounts.removeAccount(email);

        // If no accounts left, redirect to login
        if (sessionAccounts.getAccounts().isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        // Otherwise, switch to first available account
        setActiveEmail(session, sessionAccounts.getActiveAccount());
        return "redirect:/home";
    }

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

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
