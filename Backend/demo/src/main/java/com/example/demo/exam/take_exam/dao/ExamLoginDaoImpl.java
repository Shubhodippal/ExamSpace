package com.example.demo.exam.take_exam.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.demo.exam.take_exam.model.ExamLogin;

@Repository
public class ExamLoginDaoImpl implements ExamLoginDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RowMapper<ExamLogin> examLoginRowMapper = (ResultSet rs, int rowNum) -> {
        ExamLogin examLogin = new ExamLogin();
        examLogin.setId(rs.getLong("id"));
        examLogin.setUid(rs.getString("uid"));
        examLogin.setName(rs.getString("name"));
        examLogin.setEmail(rs.getString("email"));
        examLogin.setUsername(rs.getString("username"));
        examLogin.setRoll(rs.getString("roll"));
        examLogin.setExamUid(rs.getString("exam_uid"));
        examLogin.setSubmitted(rs.getString("submitted"));
        
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            examLogin.setLastLogin(lastLogin.toLocalDateTime());
        }
        
        return examLogin;
    };
    
    @Override
    public ExamLogin createExamLogin(ExamLogin examLogin) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO exam_login (uid, name, email, username, roll, exam_uid, submitted, last_login) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            
            ps.setString(1, examLogin.getUid());
            ps.setString(2, examLogin.getName());
            ps.setString(3, examLogin.getEmail());
            
            if (examLogin.getUsername() != null) {
                ps.setString(4, examLogin.getUsername());
            } else {
                ps.setNull(4, java.sql.Types.VARCHAR);
            }
            
            if (examLogin.getRoll() != null) {
                ps.setString(5, examLogin.getRoll());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }
            
            ps.setString(6, examLogin.getExamUid());
            ps.setString(7, examLogin.getSubmitted() != null ? examLogin.getSubmitted() : "NO");
            
            LocalDateTime lastLogin = examLogin.getLastLogin();
            if (lastLogin != null) {
                ps.setTimestamp(8, Timestamp.valueOf(lastLogin));
            } else {
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            }
            
            return ps;
        }, keyHolder);
        
        Long id = keyHolder.getKey().longValue();
        examLogin.setId(id);
        
        return examLogin;
    }
    
    @Override
    public ExamLogin getExamLoginByExamAndUser(String examUid, String uid) {
        try {
            String sql = "SELECT * FROM exam_login WHERE exam_uid = ? AND uid = ?";
            List<ExamLogin> results = jdbcTemplate.query(sql, examLoginRowMapper, examUid, uid);
            return results.isEmpty() ? null : results.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve exam login: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<ExamLogin> getExamLoginsByExam(String examUid) {
        try {
            String sql = "SELECT * FROM exam_login WHERE exam_uid = ? ORDER BY last_login DESC";
            return jdbcTemplate.query(sql, examLoginRowMapper, examUid);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve exam logins by exam: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean markAsSubmitted(String examUid, String uid) {
        try {
            String sql = "UPDATE exam_login SET submitted = 'YES', last_login = ? WHERE exam_uid = ? AND uid = ?";
            int rowsAffected = jdbcTemplate.update(sql, 
                Timestamp.valueOf(LocalDateTime.now()),
                examUid,
                uid
            );
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to mark exam as submitted: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean updateLastLogin(String examUid, String uid) {
        try {
            String sql = "UPDATE exam_login SET last_login = ? WHERE exam_uid = ? AND uid = ?";
            int rowsAffected = jdbcTemplate.update(sql, 
                Timestamp.valueOf(LocalDateTime.now()),
                examUid,
                uid
            );
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to update last login: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean updateSubmissionStatus(String examUid, String uid, String status) {
        try {
            String sql = "UPDATE exam_login SET submitted = ? WHERE exam_uid = ? AND uid = ?";
            int rowsAffected = jdbcTemplate.update(sql, status, examUid, uid);
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to update submission status: " + e.getMessage(), e);
        }
    }
}