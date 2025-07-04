package com.example.demo.exam;

import java.util.List;

public interface ExamDao {
    /**
     * Create a new exam
     * @param exam The exam to create
     * @return The created exam with id populated
     */
    Exams createExam(Exams exam);
    
    /**
     * Get an exam by its ID
     * @param examId The unique exam identifier
     * @return The exam or null if not found
     */
    Exams getExamById(String examId);
    
    /**
     * Get all exams created by a specific user
     * @param creatorUid The user ID of the creator
     * @return List of exams
     */
    List<Exams> getExamsByCreator(String creatorUid);
    
    /**
     * Update an existing exam
     * @param exam The exam with updated fields
     * @return true if successful, false otherwise
     */
    boolean updateExam(Exams exam);
    
    /**
     * Delete an exam
     * @param examId The unique exam identifier
     * @return true if successful, false otherwise
     */
    boolean deleteExam(String examId);
    
    /**
     * Get all exams shared with a specific email
     * @param email The email address
     * @return List of exams shared with the email
     */
    List<Exams> getExamsSharedWithEmail(String email);
}
