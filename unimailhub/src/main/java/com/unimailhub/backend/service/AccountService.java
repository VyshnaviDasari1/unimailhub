package com.unimailhub.backend.service;

import com.unimailhub.backend.entity.User;
import com.unimailhub.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {

    private final UserRepository userRepository;

    public AccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    // Session-based account management
    public static class SessionAccounts {
        private List<String> accounts = new ArrayList<>();
        private String activeAccount;

        public List<String> getAccounts() {
            return accounts;
        }

        public void setAccounts(List<String> accounts) {
            this.accounts = accounts;
        }

        public String getActiveAccount() {
            return activeAccount;
        }

        public void setActiveAccount(String activeAccount) {
            this.activeAccount = activeAccount;
        }

        public void addAccount(String email) {
            if (!accounts.contains(email)) {
                accounts.add(email);
            }
        }

        public void removeAccount(String email) {
            accounts.remove(email);
            if (activeAccount != null && activeAccount.equals(email)) {
                activeAccount = accounts.isEmpty() ? null : accounts.get(0);
            }
        }

        public boolean hasAccount(String email) {
            return accounts.contains(email);
        }
    }
}