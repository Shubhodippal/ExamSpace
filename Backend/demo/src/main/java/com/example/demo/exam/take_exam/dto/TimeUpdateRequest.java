package com.example.demo.exam.take_exam.dto;

public class TimeUpdateRequest {
    private String uid;
    private String examUid;
    private Integer timeLeft;
    
    public String getUid() {
        return uid;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public String getExamUid() {
        return examUid;
    }
    
    public void setExamUid(String examUid) {
        this.examUid = examUid;
    }
    
    public Integer getTimeLeft() {
        return timeLeft;
    }
    
    public void setTimeLeft(Integer timeLeft) {
        this.timeLeft = timeLeft;
    }
}