package com.example.demo.exam;

import java.util.Map;

public class ContentQuestionRequest {
    private String examUid;
    private String creatorUid;
    private Integer numberOfQuestions;
    private String difficulty;
    private Map<String, DocumentService.FileExtractionResult> uploadResults;
    
    // Getters and setters
    public String getExamUid() {
        return examUid;
    }
    
    public void setExamUid(String examUid) {
        this.examUid = examUid;
    }
    
    public String getCreatorUid() {
        return creatorUid;
    }
    
    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }
    
    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }
    
    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public Map<String, DocumentService.FileExtractionResult> getUploadResults() {
        return uploadResults;
    }
    
    public void setUploadResults(Map<String, DocumentService.FileExtractionResult> uploadResults) {
        this.uploadResults = uploadResults;
    }
}