package com.example.demo.exam;

import java.time.LocalDateTime;

public class Question {
    private Long id;
    private String questionUid;
    private String creatorUid;
    private String examUid;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAns;
    private LocalDateTime createdAt;
    
    // Default constructor
    public Question() {
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getQuestionUid() {
        return questionUid;
    }
    
    public void setQuestionUid(String questionUid) {
        this.questionUid = questionUid;
    }
    
    public String getCreatorUid() {
        return creatorUid;
    }
    
    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }
    
    public String getExamUid() {
        return examUid;
    }
    
    public void setExamUid(String examUid) {
        this.examUid = examUid;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getOptionA() {
        return optionA;
    }
    
    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }
    
    public String getOptionB() {
        return optionB;
    }
    
    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }
    
    public String getOptionC() {
        return optionC;
    }
    
    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }
    
    public String getOptionD() {
        return optionD;
    }
    
    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }
    
    public String getCorrectAns() {
        return correctAns;
    }
    
    public void setCorrectAns(String correctAns) {
        this.correctAns = correctAns;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}