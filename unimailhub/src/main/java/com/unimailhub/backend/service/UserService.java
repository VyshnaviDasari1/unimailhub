package com.unimailhub.backend.service;

import com.unimailhub.backend.entity.User;
import com.unimailhub.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public String register(User user) {
        if (repo.findByEmail(user.getEmail()) != null) {
            return "Email already exists";
        }
        repo.save(user);
        return "success";
    }

    public String login(User user) {
        User existing = repo.findByEmail(user.getEmail());
        if (existing == null) return "Invalid credentials";
        if (!existing.getPassword().equals(user.getPassword())) return "Invalid credentials";
        return "success";
    }
}
