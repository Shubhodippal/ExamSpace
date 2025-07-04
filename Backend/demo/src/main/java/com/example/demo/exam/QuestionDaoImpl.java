package com.example.demo.exam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class QuestionDaoImpl implements QuestionDao {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RowMapper<Question> questionRowMapper = (ResultSet rs, int rowNum) -> {
        Question question = new Question();
        question.setId(rs.getLong("id"));
        question.setQuestionUid(rs.getString("question_uid"));
        question.setCreatorUid(rs.getString("creator_uid"));
        question.setExamUid(rs.getString("exam_uid"));
        question.setQuestion(rs.getString("question"));
        question.setOptionA(rs.getString("option_a"));
        question.setOptionB(rs.getString("option_b"));
        question.setOptionC(rs.getString("option_c"));
        question.setOptionD(rs.getString("option_d"));
        question.setCorrectAns(rs.getString("correct_ans"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            question.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return question;
    };
    
    @Override
    public Question createQuestion(Question question) {
        if (question.getQuestionUid() == null || question.getQuestionUid().isEmpty()) {
            question.setQuestionUid(UUID.randomUUID().toString());
        }
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO questions (question_uid, creator_uid, exam_uid, question, " +
                "option_a, option_b, option_c, option_d, correct_ans) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            
            ps.setString(1, question.getQuestionUid());
            ps.setString(2, question.getCreatorUid());
            ps.setString(3, question.getExamUid());
            ps.setString(4, question.getQuestion());
            ps.setString(5, question.getOptionA());
            ps.setString(6, question.getOptionB());
            ps.setString(7, question.getOptionC());
            ps.setString(8, question.getOptionD());
            ps.setString(9, question.getCorrectAns());
            
            return ps;
        }, keyHolder);
        
        Long id = keyHolder.getKey().longValue();
        question.setId(id);
        
        return question;
    }
    
    @Override
    public List<Question> getQuestionsByExamUid(String examUid) {
        String sql = "SELECT * FROM questions WHERE exam_uid = ?";
        return jdbcTemplate.query(sql, questionRowMapper, examUid);
    }
    
    @Override
    public Question getQuestionById(String questionUid) {
        String sql = "SELECT * FROM questions WHERE question_uid = ?";
        List<Question> questions = jdbcTemplate.query(sql, questionRowMapper, questionUid);
        return questions.isEmpty() ? null : questions.get(0);
    }
    
    @Override
    public boolean updateQuestion(Question question) {
        String sql = "UPDATE questions SET question = ?, option_a = ?, option_b = ?, " +
                     "option_c = ?, option_d = ?, correct_ans = ? WHERE question_uid = ?";
        
        int rowsAffected = jdbcTemplate.update(sql,
            question.getQuestion(),
            question.getOptionA(),
            question.getOptionB(),
            question.getOptionC(),
            question.getOptionD(),
            question.getCorrectAns(),
            question.getQuestionUid()
        );
        
        return rowsAffected > 0;
    }
    
    @Override
    public boolean deleteQuestion(String questionUid) {
        String sql = "DELETE FROM questions WHERE question_uid = ?";
        int rowsAffected = jdbcTemplate.update(sql, questionUid);
        return rowsAffected > 0;
    }
}