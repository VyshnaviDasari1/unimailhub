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

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(User user, HttpSession session, Model model) {
        String result = userService.login(user);
        if (!result.equals("success")) {
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
        return "register";   // stays on register page with error
        }

        return "redirect:/login"; // go to login after successful registration
    }



    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @PostMapping("/send")
    public String sendMail(Mail mail, HttpSession session) {
        String fromEmail = (String) session.getAttribute("email");
        mail.setFromEmail(fromEmail);
        mailService.sendMail(mail);
        return "redirect:/home";
    }

    @GetMapping("/inbox")
    public String inbox(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        model.addAttribute("mails", mailService.inbox(email));
        return "inbox";
    }

    @GetMapping("/sent")
    public String sent(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        model.addAttribute("mails", mailService.sent(email));
        return "sent";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();   // clears logged-in user
        return "redirect:/login";
    }

    @GetMapping("/mail/{id}")
    public String readMail(@PathVariable Long id, Model model) {
        Mail mail = mailService.getMail(id);
        model.addAttribute("mail", mail);
        return "read-mail";
    }

    @GetMapping("/star/{id}")
    public String starMail(@PathVariable Long id) {
        mailService.toggleStar(id);
        return "redirect:/inbox";
    }

    @GetMapping("/starred")
    public String starred(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        model.addAttribute("mails", mailService.starred(email));
        return "starred";
    }


}

