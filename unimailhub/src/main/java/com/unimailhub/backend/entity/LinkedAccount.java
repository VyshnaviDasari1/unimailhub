package com.unimailhub.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "linked_accounts")
public class LinkedAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerEmail;   // logged-in user
    private String linkedEmail;
    private String password;

    // getters & setters
    public Long getId() { return id; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public String getLinkedEmail() { return linkedEmail; }
    public void setLinkedEmail(String linkedEmail) { this.linkedEmail = linkedEmail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
