package com.example.demo.exam;

import java.time.LocalDateTime;

public class ExamRequest {
    private String examName;
    private String creatorUid;  // Add this field to receive user ID directly
    private Integer marks;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer timeLimit;
    private String examPasscode;
    private String state;
    private String sharing;

    // Getters and setters
    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getExamPasscode() {
        return examPasscode;
    }

    public void setExamPasscode(String examPasscode) {
        this.examPasscode = examPasscode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSharing() {
        return sharing;
    }

    public void setSharing(String sharing) {
        this.sharing = sharing;
    }
}