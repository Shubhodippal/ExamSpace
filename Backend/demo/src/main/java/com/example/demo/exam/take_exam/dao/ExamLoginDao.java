package com.example.demo.exam.take_exam.dao;

import java.util.List;

import com.example.demo.exam.take_exam.model.ExamLogin;

public interface ExamLoginDao {
    /**
     * Create a new exam login record
     * @param examLogin The exam login record to create
     * @return The created exam login record with ID populated
     */
    ExamLogin createExamLogin(ExamLogin examLogin);
    
    /**
     * Get an exam login record by exam UID and user UID
     * @param examUid The exam UID
     * @param uid The user UID
     * @return The exam login record or null if not found
     */
    ExamLogin getExamLoginByExamAndUser(String examUid, String uid);
    
    /**
     * Get all exam login records for a specific exam
     * @param examUid The exam UID
     * @return List of exam login records
     */
    List<ExamLogin> getExamLoginsByExam(String examUid);
    
    /**
     * Mark an exam as submitted
     * @param examUid The exam UID
     * @param uid The user UID
     * @return true if successful, false otherwise
     */
    boolean markAsSubmitted(String examUid, String uid);
    
    /**
     * Update last login time
     * @param examUid The exam UID
     * @param uid The user UID
     * @return true if successful, false otherwise
     */
    boolean updateLastLogin(String examUid, String uid);

    /**
     * Update the submission status of an exam login
     * @param examUid The exam UID
     * @param uid The user UID
     * @param status The submission status to be updated
     * @return true if successful, false otherwise
     */
    boolean updateSubmissionStatus(String examUid, String uid, String status);
}