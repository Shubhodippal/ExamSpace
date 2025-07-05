package com.example.demo.exam.take_exam.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exam.ExamDao;
import com.example.demo.exam.Exams;
import com.example.demo.exam.Question;
import com.example.demo.exam.QuestionDao;
import com.example.demo.exam.take_exam.dao.ExamLoginDao;
import com.example.demo.exam.take_exam.dto.ExamRegistrationRequest;
import com.example.demo.exam.take_exam.dto.ExamSubmitRequest;
import com.example.demo.exam.take_exam.model.ExamLogin;

@RestController
@RequestMapping("/take-exam")
public class TakeExamController {

    @Autowired
    private ExamLoginDao examLoginDao;
    
    @Autowired
    private ExamDao examDao;
    
    @Autowired
    private QuestionDao questionDao;
    
    /**
     * Register a user for an exam and start the exam
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerForExam(@RequestBody ExamRegistrationRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (request == null) {
                response.put("status", "error");
                response.put("message", "Request cannot be null");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate required fields
            if (request.getUid() == null || request.getUid().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Name is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getExamUid() == null || request.getExamUid().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Exam ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if exam exists
            Exams exam = examDao.getExamById(request.getExamUid().trim());
            if (exam == null) {
                response.put("status", "error");
                response.put("message", "Exam not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Check if exam is ON or OFF
            if (!"ON".equals(exam.getState())) {
                response.put("status", "error");
                response.put("message", "This exam is not currently accepting responses");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Check if user is already registered for this exam
            ExamLogin existingLogin = examLoginDao.getExamLoginByExamAndUser(
                request.getExamUid().trim(), 
                request.getUid().trim()
            );
            
            if (existingLogin != null) {
                // User is already registered - check submission status
                if ("YES".equals(existingLogin.getSubmitted())) {
                    response.put("status", "error");
                    response.put("message", "You have already submitted this exam");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                }
                
                // Update last login time
                examLoginDao.updateLastLogin(request.getExamUid().trim(), request.getUid().trim());
                
                // Get questions for this exam
                List<Question> questions = questionDao.getQuestionsByExamUid(request.getExamUid().trim());
                
                if (questions.isEmpty()) {
                    response.put("status", "error");
                    response.put("message", "No questions found for this exam");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                
                // Remove correct answers from questions for security
                questions.forEach(q -> q.setCorrectAns(null));
                
                response.put("status", "success");
                response.put("message", "Resuming exam session");
                response.put("examDetails", exam);
                response.put("questions", questions);
                
                return ResponseEntity.ok(response);
                
            } else {
                // Create new exam login record
                ExamLogin examLogin = new ExamLogin();
                examLogin.setUid(request.getUid().trim());
                examLogin.setName(request.getName().trim());
                examLogin.setEmail(request.getEmail().trim().toLowerCase());
                examLogin.setUsername(request.getUsername() != null ? request.getUsername().trim() : null);
                examLogin.setRoll(request.getRoll() != null ? request.getRoll().trim() : null);
                examLogin.setExamUid(request.getExamUid().trim());
                examLogin.setSubmitted("NO");
                examLogin.setLastLogin(LocalDateTime.now());
                
                examLogin = examLoginDao.createExamLogin(examLogin);
                
                // Get questions for this exam
                List<Question> questions = questionDao.getQuestionsByExamUid(request.getExamUid().trim());
                
                if (questions.isEmpty()) {
                    response.put("status", "error");
                    response.put("message", "No questions found for this exam");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                
                // Remove correct answers from questions for security
                questions.forEach(q -> q.setCorrectAns(null));
                
                response.put("status", "success");
                response.put("message", "Successfully registered for exam");
                response.put("examDetails", exam);
                response.put("questions", questions);
                
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to register for exam: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Submit an exam
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitExam(@RequestBody ExamSubmitRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (request == null) {
                response.put("status", "error");
                response.put("message", "Request cannot be null");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate required fields
            if (request.getUid() == null || request.getUid().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (request.getExamUid() == null || request.getExamUid().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Exam ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if user is registered for this exam
            ExamLogin examLogin = examLoginDao.getExamLoginByExamAndUser(
                request.getExamUid().trim(), 
                request.getUid().trim()
            );
            
            if (examLogin == null) {
                response.put("status", "error");
                response.put("message", "User is not registered for this exam");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Check if exam is already submitted
            if ("YES".equals(examLogin.getSubmitted())) {
                response.put("status", "error");
                response.put("message", "Exam has already been submitted");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // Update submission status
            boolean updated = examLoginDao.updateSubmissionStatus(
                request.getExamUid().trim(), 
                request.getUid().trim(), 
                "YES"
            );
            
            if (updated) {
                // Update last login time
                examLoginDao.updateLastLogin(request.getExamUid().trim(), request.getUid().trim());
                
                response.put("status", "success");
                response.put("message", "Exam submitted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to submit exam");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to submit exam: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get exam status for a user
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getExamStatus(
            @RequestParam String examUid,
            @RequestParam String uid) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (examUid == null || examUid.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Exam ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (uid == null || uid.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if user is registered for this exam
            ExamLogin examLogin = examLoginDao.getExamLoginByExamAndUser(
                examUid.trim(), 
                uid.trim()
            );
            
            if (examLogin == null) {
                response.put("status", "error");
                response.put("message", "User is not registered for this exam");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Get exam details
            Exams exam = examDao.getExamById(examUid.trim());
            
            response.put("status", "success");
            response.put("examLogin", examLogin);
            response.put("examDetails", exam);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to get exam status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}