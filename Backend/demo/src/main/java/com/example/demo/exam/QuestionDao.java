package com.example.demo.exam;

import java.util.List;

public interface QuestionDao {
    Question createQuestion(Question question);
    List<Question> getQuestionsByExamUid(String examUid);
    Question getQuestionById(String questionUid);
    boolean updateQuestion(Question question);
    boolean deleteQuestion(String questionUid);
}