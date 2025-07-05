package com.example.demo.exam.take_exam.model;

import java.time.LocalDateTime;

public class ExamLogin {
    private Long id;
    private String uid;
    private String name;
    private String email;
    private String username;
    private String roll;
    private String examUid;
    private String submitted; // "YES" or "NO"
    private LocalDateTime lastLogin;
    
    // Default constructor
    public ExamLogin() {
        this.submitted = "NO";
        this.lastLogin = LocalDateTime.now();
    }
    
    // Constructor with required fields
    public ExamLogin(String uid, String name, String email, String examUid) {
        this();
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.examUid = examUid;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUid() {
        return uid;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRoll() {
        return roll;
    }
    
    public void setRoll(String roll) {
        this.roll = roll;
    }
    
    public String getExamUid() {
        return examUid;
    }
    
    public void setExamUid(String examUid) {
        this.examUid = examUid;
    }
    
    public String getSubmitted() {
        return submitted;
    }
    
    public void setSubmitted(String submitted) {
        this.submitted = submitted;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}