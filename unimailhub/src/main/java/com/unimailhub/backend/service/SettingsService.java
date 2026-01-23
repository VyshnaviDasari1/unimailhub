package com.unimailhub.backend.service;

import com.unimailhub.backend.entity.LinkedAccount;
import com.unimailhub.backend.repository.LinkedAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsService {

    private final LinkedAccountRepository linkedAccountRepository;

    public SettingsService(LinkedAccountRepository linkedAccountRepository) {
        this.linkedAccountRepository = linkedAccountRepository;
    }

    // ✅ Save linked account
    public void addLinkedAccount(String ownerEmail,
                                 String linkedEmail,
                                 String password) {

        LinkedAccount account = new LinkedAccount();
        account.setOwnerEmail(ownerEmail);
        account.setLinkedEmail(linkedEmail);
        account.setPassword(password);

        linkedAccountRepository.save(account);
    }

    // ✅ Fetch linked accounts for UI
    public List<LinkedAccount> getLinkedAccounts(String ownerEmail) {
        return linkedAccountRepository.findByOwnerEmail(ownerEmail);
    }
}
