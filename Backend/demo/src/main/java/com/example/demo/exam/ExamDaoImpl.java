package com.example.demo.exam;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ExamDaoImpl implements ExamDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RowMapper<Exams> examRowMapper = (ResultSet rs, int rowNum) -> {
        Exams exam = new Exams();
        exam.setId(rs.getLong("id"));
        exam.setExamId(rs.getString("exam_id"));
        exam.setCreatorUid(rs.getString("creator_uid"));
        exam.setMarks(rs.getObject("marks", Integer.class));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            exam.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        // Remove start_at, end_at, time_limit fields
        exam.setState(rs.getString("state"));
        exam.setExamName(rs.getString("exam_name"));
        exam.setExamPasscode(rs.getString("exam_passcode"));
        exam.setSharing(rs.getString("sharing"));
        
        return exam;
    };
    
    @Override
    public Exams createExam(Exams exam) {
        if (exam.getExamId() == null || exam.getExamId().isEmpty()) {
            exam.setExamId(UUID.randomUUID().toString());
        }
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO exam (exam_id, creator_uid, marks, state, exam_name, exam_passcode, sharing) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            
            ps.setString(1, exam.getExamId());
            ps.setString(2, exam.getCreatorUid());
            
            if (exam.getMarks() != null) {
                ps.setInt(3, exam.getMarks());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            
            ps.setString(4, exam.getState() != null ? exam.getState() : "OFF");
            ps.setString(5, exam.getExamName());
            ps.setString(6, exam.getExamPasscode());
            ps.setString(7, exam.getSharing());
            
            return ps;
        }, keyHolder);
        
        Long id = keyHolder.getKey().longValue();
        exam.setId(id);
        
        return exam;
    }
    
    @Override
    public Exams getExamById(String examId) {
        String sql = "SELECT * FROM exam WHERE exam_id = ?";
        List<Exams> exams = jdbcTemplate.query(sql, examRowMapper, examId);
        return exams.isEmpty() ? null : exams.get(0);
    }
    
    @Override
    public List<Exams> getExamsByCreator(String creatorUid) {
        String sql = "SELECT * FROM exam WHERE creator_uid = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, examRowMapper, creatorUid);
    }
    
    @Override
    public boolean updateExam(Exams exam) {
        String sql = "UPDATE exam SET marks = ?, state = ?, exam_name = ?, exam_passcode = ?, sharing = ? WHERE exam_id = ?";
        
        int rowsAffected = jdbcTemplate.update(sql,
            exam.getMarks(),
            exam.getState(),
            exam.getExamName(),
            exam.getExamPasscode(),
            exam.getSharing(),
            exam.getExamId()
        );
        
        return rowsAffected > 0;
    }
    
    @Override
    public boolean deleteExam(String examId) {
        String sql = "DELETE FROM exam WHERE exam_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, examId);
        return rowsAffected > 0;
    }
    
    @Override
    public List<Exams> getExamsSharedWithEmail(String email) {
        // Improved query to handle all possible positions in a large sharing list
        String sql = "SELECT * FROM exam WHERE " +
                    "sharing = ? OR " +                      // Exact match (only this email)
                    "sharing LIKE ? OR " +                   // At the beginning of list
                    "sharing LIKE ? OR " +                   // At the end with space
                    "sharing LIKE ? OR " +                   // At the end without space
                    "sharing LIKE ? OR " +                   // In the middle with spaces on both sides
                    "sharing LIKE ?";                        // In the middle without spaces

        String emailTrimmed = email.trim();
        
        return jdbcTemplate.query(
                sql, 
                examRowMapper, 
                emailTrimmed,                        // Exact match
                emailTrimmed + ",%",                 // Beginning of list 
                "%, " + emailTrimmed,               // End with space after comma
                "%," + emailTrimmed,                // End without space
                "%, " + emailTrimmed + ",%",        // Middle with spaces
                "%," + emailTrimmed + ",%"          // Middle without spaces
        );
    }
}
