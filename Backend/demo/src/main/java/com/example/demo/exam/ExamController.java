package com.example.demo.exam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.User.UserDao;


@RestController
@RequestMapping("/exam")
public class ExamController {
    
    @Autowired
    private DocumentService documentService;
    
    @Autowired
    private ExamDao examDao;
    
    @Autowired
    private UserDao userDao;
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @RequestParam(value = "file", required = false) MultipartFile singleFile,
            @RequestParam(value = "files", required = false) List<MultipartFile> multipleFiles,
            @RequestParam(value = "title", required = false) String title) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (singleFile != null && !singleFile.isEmpty()) {
                return processMultipleFiles(List.of(singleFile));
            } else if (multipleFiles != null && !multipleFiles.isEmpty()) {
                return processMultipleFiles(multipleFiles);
            } else {
                response.put("status", "error");
                response.put("message", "No files were uploaded");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", "File processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (RuntimeException e) {
            response.put("status", "error");
            response.put("message", "A runtime error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    private ResponseEntity<Map<String, Object>> processMultipleFiles(List<MultipartFile> files) {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, DocumentService.FileExtractionResult> results = documentService.extractContentFromMultipleFiles(files);
        
        long successCount = results.values().stream().filter(DocumentService.FileExtractionResult::isSuccess).count();
        
        long failCount = results.size() - successCount;
        
        response.put("status", "success");
        response.put("message", String.format("Processed %d files (%d successful, %d failed)", results.size(), successCount, failCount));
        response.put("results", results);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a new exam
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createExam(@RequestBody ExamRequest examRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate required fields
            if (examRequest.getExamName() == null || examRequest.getExamName().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Exam name is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (examRequest.getCreatorUid() == null || examRequest.getCreatorUid().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Creator UID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Create and populate the exam object
            Exams exam = new Exams();
            exam.setExamName(examRequest.getExamName());
            exam.setCreatorUid(examRequest.getCreatorUid());
            exam.setMarks(examRequest.getMarks());
            exam.setStartAt(examRequest.getStartAt());
            exam.setEndAt(examRequest.getEndAt());
            exam.setTimeLimit(examRequest.getTimeLimit());
            exam.setExamPasscode(examRequest.getExamPasscode());
            exam.setState("OFF"); // Default to OFF
            exam.setSharing(examRequest.getSharing()); // Add this line to handle the sharing field
            
            // Save the exam
            Exams createdExam = examDao.createExam(exam);
            
            response.put("status", "success");
            response.put("message", "Exam created successfully");
            response.put("exam", createdExam);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create exam: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all exams created by a specific user
     */
    @GetMapping("/my-exams/{userId}")
    public ResponseEntity<Map<String, Object>> getMyExams(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userToQuery;
            
            if (userId != null && !userId.trim().isEmpty()) {
                // Use the provided user ID
                userToQuery = userId;
            } else {
                // Fall back to current authenticated user if no userId provided
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                userToQuery = authentication.getName();
            }
            
            // Get exams created by this user
            List<Exams> exams = examDao.getExamsByCreator(userToQuery);
            
            response.put("status", "success");
            response.put("exams", exams);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to retrieve exams: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get a specific exam by ID
     */
    @GetMapping("/{examId}")
    public ResponseEntity<Map<String, Object>> getExamById(@PathVariable String examId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Exams exam = examDao.getExamById(examId);
            
            if (exam == null) {
                response.put("status", "error");
                response.put("message", "Exam not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.put("status", "success");
            response.put("exam", exam);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to retrieve exam: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update an existing exam
     */
    @PutMapping("/{examId}")
    public ResponseEntity<Map<String, Object>> updateExam(
            @PathVariable String examId,
            @RequestBody ExamRequest examRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if the exam exists
            Exams existingExam = examDao.getExamById(examId);
            if (existingExam == null) {
                response.put("status", "error");
                response.put("message", "Exam not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Update exam fields
            if (examRequest.getExamName() != null && !examRequest.getExamName().trim().isEmpty()) {
                existingExam.setExamName(examRequest.getExamName());
            }
            
            if (examRequest.getMarks() != null) {
                existingExam.setMarks(examRequest.getMarks());
            }
            
            if (examRequest.getStartAt() != null) {
                existingExam.setStartAt(examRequest.getStartAt());
            }
            
            if (examRequest.getEndAt() != null) {
                existingExam.setEndAt(examRequest.getEndAt());
            }
            
            if (examRequest.getTimeLimit() != null) {
                existingExam.setTimeLimit(examRequest.getTimeLimit());
            }
            
            if (examRequest.getExamPasscode() != null) {
                existingExam.setExamPasscode(examRequest.getExamPasscode());
            }
            
            // Add handling for the sharing field
            if (examRequest.getSharing() != null) {
                existingExam.setSharing(examRequest.getSharing());
            }
            
            // Add ability to update exam state (ON/OFF)
            if (examRequest.getState() != null) {
                String state = examRequest.getState().trim().toUpperCase();
                if (state.equals("ON") || state.equals("OFF")) {
                    existingExam.setState(state);
                } else {
                    response.put("status", "error");
                    response.put("message", "Invalid state value. Must be either 'ON' or 'OFF'");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
            
            // Save the updated exam
            boolean updated = examDao.updateExam(existingExam);
            
            if (updated) {
                response.put("status", "success");
                response.put("message", "Exam updated successfully");
                response.put("exam", existingExam);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to update exam");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update exam: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Autowired
    private QuestionDao questionDao;
    
    @Autowired
    private CohereService cohereService;
    
    /**
     * Generate questions for an exam using Cohere AI
     */
    @PostMapping("/generate-questions")
    public ResponseEntity<Map<String, Object>> generateQuestions(@RequestBody QuestionGenerationRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate request
            if (request.getExamUid() == null || request.getExamUid().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Exam UID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (request.getCreatorUid() == null || request.getCreatorUid().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Creator UID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (request.getNumberOfQuestions() == null || request.getNumberOfQuestions() <= 0) {
                response.put("status", "error");
                response.put("message", "Number of questions must be greater than 0");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Topic is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // New validation for subject
            if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Subject is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Check if the exam exists
            Exams exam = examDao.getExamById(request.getExamUid());
            if (exam == null) {
                response.put("status", "error");
                response.put("message", "Exam not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Generate questions in multiple batches until we reach the requested count
            int requestedQuestions = request.getNumberOfQuestions();
            int maxBatchSize = 25; // Maximum questions per batch
            int remainingQuestions = requestedQuestions;
            List<Question> allGeneratedQuestions = new ArrayList<>();
            int maxAttempts = 10; // Prevent infinite loop
            int attempts = 0;
            
            while (remainingQuestions > 0 && attempts < maxAttempts) {
                attempts++;
                int currentBatchSize = Math.min(remainingQuestions, maxBatchSize);
                
                // Updated call to cohereService with new parameters
                List<Question> batchQuestions = cohereService.generateQuestions(
                    request.getSubject(),
                    request.getTopic(), 
                    request.getSpecificAreas(),
                    currentBatchSize,
                    request.getDifficulty()
                );
                
                if (batchQuestions != null && !batchQuestions.isEmpty()) {
                    allGeneratedQuestions.addAll(batchQuestions);
                    remainingQuestions -= batchQuestions.size();
                    System.out.println("Generated " + batchQuestions.size() + " questions. Remaining: " + remainingQuestions);
                } else {
                    // Short delay before retry
                    Thread.sleep(2000);
                }
                
                // Add a delay between API calls to avoid rate limiting
                if (remainingQuestions > 0) {
                    Thread.sleep(2000);
                }
            }
            
            // Save questions to database with validation
            List<Question> savedQuestions = new ArrayList<>();
            for (Question question : allGeneratedQuestions) {
                question.setExamUid(request.getExamUid());
                question.setCreatorUid(request.getCreatorUid());
                question.setQuestionUid(UUID.randomUUID().toString());
                
                // Ensure all required fields are present
                if (question.getQuestion() == null || question.getOptionA() == null || 
                    question.getOptionB() == null || question.getOptionC() == null || 
                    question.getOptionD() == null) {
                    continue; // Skip incomplete questions
                }
                
                // Ensure correctAns is valid
                if (question.getCorrectAns() == null || 
                    !(question.getCorrectAns().equals("A") || 
                      question.getCorrectAns().equals("B") || 
                      question.getCorrectAns().equals("C") || 
                      question.getCorrectAns().equals("D"))) {
                    
                    // If we have options, let's try to make an intelligent guess about the correct answer
                    if (question.getOptionA() != null && question.getOptionA().toLowerCase().contains("correct") ||
                        question.getOptionA() != null && question.getOptionA().toLowerCase().contains("true")) {
                        question.setCorrectAns("A");
                    } else if (question.getOptionB() != null && question.getOptionB().toLowerCase().contains("correct") ||
                               question.getOptionB() != null && question.getOptionB().toLowerCase().contains("true")) {
                        question.setCorrectAns("B");
                    } else if (question.getOptionC() != null && question.getOptionC().toLowerCase().contains("correct") ||
                               question.getOptionC() != null && question.getOptionC().toLowerCase().contains("true")) {
                        question.setCorrectAns("C");
                    } else if (question.getOptionD() != null && question.getOptionD().toLowerCase().contains("correct") ||
                               question.getOptionD() != null && question.getOptionD().toLowerCase().contains("true")) {
                        question.setCorrectAns("D");
                    } else {
                        // If we can't determine a better answer, randomly assign instead of always A
                        String[] options = {"A", "B", "C", "D"};
                        question.setCorrectAns(options[new Random().nextInt(options.length)]);
                    }
                }
                
                savedQuestions.add(questionDao.createQuestion(question));
                
                // If we've reached the requested number, stop
                if (savedQuestions.size() >= requestedQuestions) {
                    break;
                }
            }
            
            // Include info about requested vs. actual questions
            response.put("status", "success");
            response.put("message", String.format("Generated and saved %d of %d requested questions", 
                savedQuestions.size(), requestedQuestions));
            response.put("questions", savedQuestions);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to generate questions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Generate questions from content data
     */
    @PostMapping("/generate-questions-from-content")
    public ResponseEntity<Map<String, Object>> generateQuestionsFromContent(@RequestBody ContentQuestionRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate request
            if (request.getExamUid() == null || request.getExamUid().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Exam UID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (request.getCreatorUid() == null || request.getCreatorUid().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Creator UID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (request.getNumberOfQuestions() == null || request.getNumberOfQuestions() <= 0) {
                response.put("status", "error");
                response.put("message", "Number of questions must be greater than 0");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (request.getUploadResults() == null || request.getUploadResults().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Content data is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Check if the exam exists
            Exams exam = examDao.getExamById(request.getExamUid());
            if (exam == null) {
                response.put("status", "error");
                response.put("message", "Exam not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Extract content from the upload results
            StringBuilder contentBuilder = new StringBuilder();
            for (Map.Entry<String, DocumentService.FileExtractionResult> entry : request.getUploadResults().entrySet()) {
                DocumentService.FileExtractionResult result = entry.getValue();
                if (result.isSuccess() && result.getContent() != null && !result.getContent().isEmpty()) {
                    contentBuilder.append(result.getContent()).append("\n\n");
                }
            }
            
            String combinedContent = contentBuilder.toString().trim();
            if (combinedContent.isEmpty()) {
                response.put("status", "error");
                response.put("message", "No valid content found in the provided data");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Generate questions in multiple batches until we reach the requested count
            int requestedQuestions = request.getNumberOfQuestions();
            int maxBatchSize = 25; // Maximum questions per batch
            int remainingQuestions = requestedQuestions;
            List<Question> allGeneratedQuestions = new ArrayList<>();
            int maxAttempts = 10; // Prevent infinite loop
            int attempts = 0;
            
            while (remainingQuestions > 0 && attempts < maxAttempts) {
                attempts++;
                int currentBatchSize = Math.min(remainingQuestions, maxBatchSize);
                
                // Generate questions based on the provided content
                List<Question> batchQuestions = cohereService.generateQuestionsFromContent(
                    combinedContent,
                    currentBatchSize,
                    request.getDifficulty()
                );
                
                if (batchQuestions != null && !batchQuestions.isEmpty()) {
                    allGeneratedQuestions.addAll(batchQuestions);
                    remainingQuestions -= batchQuestions.size();
                    System.out.println("Generated " + batchQuestions.size() + " questions from content. Remaining: " + remainingQuestions);
                } else {
                    // Short delay before retry
                    Thread.sleep(2000);
                }
                
                // Add a delay between API calls to avoid rate limiting
                if (remainingQuestions > 0) {
                    Thread.sleep(2000);
                }
            }
            
            // Save questions to database with validation
            List<Question> savedQuestions = new ArrayList<>();
            for (Question question : allGeneratedQuestions) {
                question.setExamUid(request.getExamUid());
                question.setCreatorUid(request.getCreatorUid());
                question.setQuestionUid(UUID.randomUUID().toString());
                
                // Ensure all required fields are present
                if (question.getQuestion() == null || question.getOptionA() == null || 
                    question.getOptionB() == null || question.getOptionC() == null || 
                    question.getOptionD() == null) {
                    continue; // Skip incomplete questions
                }
                
                // Ensure correctAns is valid
                if (question.getCorrectAns() == null || 
                    !(question.getCorrectAns().equals("A") || 
                      question.getCorrectAns().equals("B") || 
                      question.getCorrectAns().equals("C") || 
                      question.getCorrectAns().equals("D"))) {
                    
                    // If we have options, let's try to make an intelligent guess about the correct answer
                    if (question.getOptionA() != null && question.getOptionA().toLowerCase().contains("correct") ||
                        question.getOptionA() != null && question.getOptionA().toLowerCase().contains("true")) {
                        question.setCorrectAns("A");
                    } else if (question.getOptionB() != null && question.getOptionB().toLowerCase().contains("correct") ||
                               question.getOptionB() != null && question.getOptionB().toLowerCase().contains("true")) {
                        question.setCorrectAns("B");
                    } else if (question.getOptionC() != null && question.getOptionC().toLowerCase().contains("correct") ||
                               question.getOptionC() != null && question.getOptionC().toLowerCase().contains("true")) {
                        question.setCorrectAns("C");
                    } else if (question.getOptionD() != null && question.getOptionD().toLowerCase().contains("correct") ||
                               question.getOptionD() != null && question.getOptionD().toLowerCase().contains("true")) {
                        question.setCorrectAns("D");
                    } else {
                        // If we can't determine a better answer, randomly assign
                        String[] options = {"A", "B", "C", "D"};
                        question.setCorrectAns(options[new Random().nextInt(options.length)]);
                    }
                }
                
                savedQuestions.add(questionDao.createQuestion(question));
                
                // If we've reached the requested number, stop
                if (savedQuestions.size() >= requestedQuestions) {
                    break;
                }
            }
            
            // Include info about requested vs. actual questions
            response.put("status", "success");
            response.put("message", String.format("Generated and saved %d of %d requested questions from content", 
                savedQuestions.size(), requestedQuestions));
            response.put("questions", savedQuestions);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to generate questions from content: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all questions for a specific exam
     */
    @GetMapping("/{examId}/questions")
    public ResponseEntity<Map<String, Object>> getQuestionsByExamId(@PathVariable String examId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if the exam exists
            Exams exam = examDao.getExamById(examId);
            if (exam == null) {
                response.put("status", "error");
                response.put("message", "Exam not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Get all questions for this exam
            List<Question> questions = questionDao.getQuestionsByExamUid(examId);
            
            response.put("status", "success");
            response.put("examId", examId);
            response.put("examName", exam.getExamName());
            response.put("questionCount", questions.size());
            response.put("questions", questions);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to retrieve questions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Update a specific question
     */
    @PutMapping("/question/{questionUid}")
    public ResponseEntity<Map<String, Object>> updateQuestion(
            @PathVariable String questionUid,
            @RequestBody Question questionRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if the question exists
            Question existingQuestion = questionDao.getQuestionById(questionUid);
            if (existingQuestion == null) {
                response.put("status", "error");
                response.put("message", "Question not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Update the question fields but keep original IDs
            existingQuestion.setQuestion(questionRequest.getQuestion());
            existingQuestion.setOptionA(questionRequest.getOptionA());
            existingQuestion.setOptionB(questionRequest.getOptionB());
            existingQuestion.setOptionC(questionRequest.getOptionC());
            existingQuestion.setOptionD(questionRequest.getOptionD());
            existingQuestion.setCorrectAns(questionRequest.getCorrectAns());
            
            // Save the updated question
            boolean updated = questionDao.updateQuestion(existingQuestion);
            
            if (updated) {
                response.put("status", "success");
                response.put("message", "Question updated successfully");
                response.put("question", existingQuestion);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to update question");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update question: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete a specific question
     */
    @DeleteMapping("/question/{questionUid}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(@PathVariable String questionUid) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if the question exists
            Question existingQuestion = questionDao.getQuestionById(questionUid);
            if (existingQuestion == null) {
                response.put("status", "error");
                response.put("message", "Question not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Delete the question
            boolean deleted = questionDao.deleteQuestion(questionUid);
            
            if (deleted) {
                response.put("status", "success");
                response.put("message", "Question deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to delete question");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete question: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Manually add a new question to an exam
     */
    @PostMapping("/add-question")
    public ResponseEntity<Map<String, Object>> addQuestion(@RequestBody Question questionRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate required fields
            if (questionRequest.getExamUid() == null || questionRequest.getExamUid().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Exam UID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (questionRequest.getCreatorUid() == null || questionRequest.getCreatorUid().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Creator UID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (questionRequest.getQuestion() == null || questionRequest.getQuestion().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Question text is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (questionRequest.getOptionA() == null || questionRequest.getOptionA().trim().isEmpty() ||
                questionRequest.getOptionB() == null || questionRequest.getOptionB().trim().isEmpty() ||
                questionRequest.getOptionC() == null || questionRequest.getOptionC().trim().isEmpty() ||
                questionRequest.getOptionD() == null || questionRequest.getOptionD().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "All four options (A, B, C, D) are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (questionRequest.getCorrectAns() == null || 
                !(questionRequest.getCorrectAns().equals("A") || 
                  questionRequest.getCorrectAns().equals("B") || 
                  questionRequest.getCorrectAns().equals("C") || 
                  questionRequest.getCorrectAns().equals("D"))) {
                response.put("status", "error");
                response.put("message", "Correct answer must be one of: A, B, C, or D");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Check if the exam exists
            Exams exam = examDao.getExamById(questionRequest.getExamUid());
            if (exam == null) {
                response.put("status", "error");
                response.put("message", "Exam not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Generate a UUID if not provided
            if (questionRequest.getQuestionUid() == null || questionRequest.getQuestionUid().trim().isEmpty()) {
                questionRequest.setQuestionUid(UUID.randomUUID().toString());
            }
            
            // Save the question
            Question savedQuestion = questionDao.createQuestion(questionRequest);
            
            response.put("status", "success");
            response.put("message", "Question added successfully");
            response.put("question", savedQuestion);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to add question: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get all exams shared with a specific email address
     */
    @GetMapping("/shared-exams")
    public ResponseEntity<Map<String, Object>> getSharedExams(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate email parameter
            if (email == null || email.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Email is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Find exams shared with this email
            List<Exams> sharedExams = examDao.getExamsSharedWithEmail(email.trim());
            
            if (sharedExams.isEmpty()) {
                response.put("status", "success");
                response.put("message", "No exams shared with this email");
                response.put("exams", new ArrayList<>());
                return ResponseEntity.ok(response);
            }
            
            // Create a list of exam details with creator information
            List<Map<String, Object>> examDetailsList = new ArrayList<>();
            for (Exams exam : sharedExams) {
                Map<String, Object> examDetails = new HashMap<>();
                // Add exam ID and name
                examDetails.put("examId", exam.getExamId());  // This is already the exam UID
                examDetails.put("examName", exam.getExamName());
                examDetails.put("examPasscode", exam.getExamPasscode());  // Add this line
                
                // Add additional exam information
                examDetails.put("status", exam.getState());  // ON/OFF
                examDetails.put("marks", exam.getMarks());
                examDetails.put("duration", exam.getTimeLimit());
                examDetails.put("createdAt", exam.getCreatedAt());
                examDetails.put("sharing", exam.getSharing());
                
                // Get creator information
                String creatorEmail = userDao.getEmailByUserId(exam.getCreatorUid());
                String creatorName = userDao.getNameByUserId(exam.getCreatorUid());
                
                examDetails.put("creatorUid", exam.getCreatorUid());
                examDetails.put("creatorEmail", creatorEmail);
                examDetails.put("creatorName", creatorName);
                
                examDetailsList.add(examDetails);
            }
            
            response.put("status", "success");
            response.put("email", email);
            response.put("count", examDetailsList.size());
            response.put("exams", examDetailsList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to retrieve shared exams: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Delete an exam and all its questions
     */
    @DeleteMapping("/{examId}")
    public ResponseEntity<Map<String, Object>> deleteExam(@PathVariable String examId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if the exam exists
            Exams exam = examDao.getExamById(examId);
            if (exam == null) {
                response.put("status", "error");
                response.put("message", "Exam not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Get all questions for this exam
            List<Question> questions = questionDao.getQuestionsByExamUid(examId);
            int questionCount = questions.size();
            
            // Delete all questions first
            boolean allQuestionsDeleted = true;
            for (Question question : questions) {
                boolean deleted = questionDao.deleteQuestion(question.getQuestionUid());
                if (!deleted) {
                    allQuestionsDeleted = false;
                }
            }
            
            // Now delete the exam
            boolean examDeleted = examDao.deleteExam(examId);
            
            if (examDeleted) {
                response.put("status", "success");
                response.put("message", "Exam deleted successfully along with " + questionCount + " questions");
                if (!allQuestionsDeleted) {
                    response.put("warning", "Some questions may not have been deleted completely");
                }
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to delete exam");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete exam: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
