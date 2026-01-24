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
import com.unimailhub.backend.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import java.util.List;



@Controller
public class AuthController {

    private final UserService userService;
    private final MailService mailService;
     private final SettingsService settingsService;
     public final UserRepository userRepository;


    public AuthController(UserService userService,
         MailService mailService, SettingsService settingsService, UserRepository userRepository) {
        this.userService = userService;
        this.mailService = mailService;
        this.settingsService = settingsService;
        this.userRepository = userRepository;
    }

    /* ===================== AUTH ===================== */

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(User user, HttpSession session, Model model) {
        String result = userService.login(user);

        if (!"success".equals(result)) {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }

        session.setAttribute("email", user.getEmail());
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

}
