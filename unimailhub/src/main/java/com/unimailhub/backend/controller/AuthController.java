package com.unimailhub.backend.controller;

import com.unimailhub.backend.entity.User;
import com.unimailhub.backend.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.unimailhub.backend.entity.Mail;
import com.unimailhub.backend.service.MailService;
import jakarta.servlet.http.HttpSession;



@Controller
public class AuthController {

    private final UserService userService;
    private final MailService mailService;

    public AuthController(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
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
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("email");

        if (email == null) {
            return "redirect:/login";
        }

        if ("starred".equals(tab)) {
            model.addAttribute("mails", mailService.starred(email));
        } else if ("sent".equals(tab)) {
            model.addAttribute("mails", mailService.sent(email));
        } else {
            model.addAttribute("mails", mailService.inbox(email));
        }

        model.addAttribute("activeTab", tab);
        return "home";
    }

    /* ===================== MAIL ACTIONS ===================== */

    @PostMapping("/send")
    public String sendMail(Mail mail, HttpSession session) {
        String fromEmail = (String) session.getAttribute("email");

        if (fromEmail == null) {
            return "redirect:/login";
        }

        mail.setFromEmail(fromEmail);
        mailService.sendMail(mail);
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
}
