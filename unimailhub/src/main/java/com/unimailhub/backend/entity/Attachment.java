package com.unimailhub.backend.entity;

import jakarta.persistence.*;

@Entity
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private String filePath;

    @ManyToOne
    private Mail mail;

    // ✅ REQUIRED setters
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setMail(Mail mail) { this.mail = mail; }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getFileName() {
        return fileName;
    }
    public String getFileType() {
        return fileType;
    }
    public String getFilePath() {
        return filePath;
    }
    public Mail getMail() {
        return mail;
    }
    
    

    // getters optional for now
}

