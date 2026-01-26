package com.unimailhub.backend.service;

import com.unimailhub.backend.entity.Alert;
import com.unimailhub.backend.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    // Create a new alert
    @Transactional
    public void createAlert(String userEmail, String message) {
        Alert alert = new Alert();
        alert.setUserEmail(userEmail);
        alert.setMessage(message);
        alertRepository.save(alert);
    }

    // Get all alerts for a user
    public List<Alert> getAlerts(String userEmail) {
        return alertRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    // Get unread alerts for a user
    public List<Alert> getUnreadAlerts(String userEmail) {
        return alertRepository.findByUserEmailAndIsReadFalseOrderByCreatedAtDesc(userEmail);
    }

    // Mark a specific alert as read
    @Transactional
    public void markAsRead(Long alertId, String userEmail) {
        alertRepository.markAsRead(alertId, userEmail);
    }

    // Mark all alerts as read for a user
    @Transactional
    public void markAllAsRead(String userEmail) {
        alertRepository.markAllAsRead(userEmail);
    }
}