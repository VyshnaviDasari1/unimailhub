package com.unimailhub.backend.service;

import com.unimailhub.backend.entity.LoginAttempt;
import com.unimailhub.backend.entity.SecurityToken;
import com.unimailhub.backend.entity.User;
import com.unimailhub.backend.repository.LoginAttemptRepository;
import com.unimailhub.backend.repository.SecurityTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SecurityService {

    private final LoginAttemptRepository loginAttemptRepo;
    private final SecurityTokenRepository tokenRepo;
    private final UserService userService;
    private final EmailService emailService;

    public SecurityService(LoginAttemptRepository loginAttemptRepo,
                          SecurityTokenRepository tokenRepo,
                          UserService userService,
                          EmailService emailService) {
        this.loginAttemptRepo = loginAttemptRepo;
        this.tokenRepo = tokenRepo;
        this.userService = userService;
        this.emailService = emailService;
    }

    public boolean isKnownDevice(User user, String ip, String userAgent) {
        return ip.equals(user.getLastKnownIP()) && userAgent.equals(user.getLastKnownUserAgent());
    }

    public LoginAttempt createPendingLoginAttempt(String email, String ip, String userAgent) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setIpAddress(ip);
        attempt.setUserAgent(userAgent);
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setStatus("pending");
        return loginAttemptRepo.save(attempt);
    }

    public void sendSecurityEmail(LoginAttempt attempt) {
        String tokenApprove = generateToken();
        String tokenDeny = generateToken();

        SecurityToken approveToken = new SecurityToken();
        approveToken.setToken(tokenApprove);
        approveToken.setEmail(attempt.getEmail());
        approveToken.setLoginAttemptId(attempt.getId());
        approveToken.setAction("approve");
        approveToken.setUsed(false);
        approveToken.setCreatedAt(LocalDateTime.now());
        approveToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepo.save(approveToken);

        SecurityToken denyToken = new SecurityToken();
        denyToken.setToken(tokenDeny);
        denyToken.setEmail(attempt.getEmail());
        denyToken.setLoginAttemptId(attempt.getId());
        denyToken.setAction("deny");
        denyToken.setUsed(false);
        denyToken.setCreatedAt(LocalDateTime.now());
        denyToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepo.save(denyToken);

        String approveUrl = "http://localhost:8080/security/approve?token=" + tokenApprove;
        String denyUrl = "http://localhost:8080/security/deny?token=" + tokenDeny;

        emailService.sendSecurityEmail(attempt.getEmail(), approveUrl, denyUrl);
    }

    public String processSecurityToken(String token, String expectedAction) {
        SecurityToken securityToken = tokenRepo.findByToken(token);
        if (securityToken == null) {
            return "Invalid token";
        }
        if (securityToken.isUsed()) {
            return "Token already used";
        }
        if (LocalDateTime.now().isAfter(securityToken.getExpiresAt())) {
            return "Token expired";
        }
        if (!expectedAction.equals(securityToken.getAction())) {
            return "Invalid action";
        }

        securityToken.setUsed(true);
        tokenRepo.save(securityToken);

        LoginAttempt attempt = loginAttemptRepo.findByIdAndStatus(securityToken.getLoginAttemptId(), "pending");
        if (attempt == null) {
            return "Login attempt not found";
        }

        if ("approve".equals(expectedAction)) {
            attempt.setStatus("approved");
            loginAttemptRepo.save(attempt);

            // Update user's known device
            User user = userService.findByEmail(attempt.getEmail());
            if (user != null) {
                user.setLastKnownIP(attempt.getIpAddress());
                user.setLastKnownUserAgent(attempt.getUserAgent());
                userService.updateUser(user);
            }

            return "approved";
        } else {
            attempt.setStatus("denied");
            loginAttemptRepo.save(attempt);
            return "denied";
        }
    }

    public boolean hasApprovedLoginAttempt(String email, String ip, String userAgent) {
        List<LoginAttempt> attempts = loginAttemptRepo.findByEmailAndStatus(email, "approved");
        return attempts.stream().anyMatch(a ->
            a.getIpAddress().equals(ip) && a.getUserAgent().equals(userAgent) &&
            a.getAttemptTime().isAfter(LocalDateTime.now().minusMinutes(30))
        );
    }

    public SecurityToken getTokenByToken(String token) {
        return tokenRepo.findByToken(token);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}