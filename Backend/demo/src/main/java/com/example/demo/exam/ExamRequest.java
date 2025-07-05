package com.example.demo.exam;

public class ExamRequest {
    private String examName;
    private String creatorUid;
    private Integer marks;
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