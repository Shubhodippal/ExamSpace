package com.example.demo.User;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl implements UserDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    private Timestamp getCurrentISTTimestamp() {
        return Timestamp.from(ZonedDateTime.now(IST_ZONE).toInstant());
    }

    @Override
    public int saveUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        // Validate required fields
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }
        
        if (user.getUserid() == null || user.getUserid().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("User password cannot be null or empty");
        }
        
        try {
            String sql = "INSERT INTO users (created_at, uid, name, phone, email, password) VALUES (?, ?, ?, ?, ?, ?)";
            return jdbcTemplate.update(sql, 
                getCurrentISTTimestamp(), 
                user.getUserid().trim(), 
                user.getName().trim(), 
                user.getPhone() != null ? user.getPhone().trim() : null, 
                user.getEmail().trim().toLowerCase(), 
                user.getPassword());
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    @Override
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        try {
            String sql = "SELECT * FROM users WHERE email = ?";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setUserid(rs.getString("uid"));
                user.setName(rs.getString("name"));
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                return user;
            }, email.trim().toLowerCase());
        } catch (EmptyResultDataAccessException e) {
            // User not found
            return null;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve user by email: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isUserIdExists(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE uid = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId.trim());
            return count != null && count > 0;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to check if user ID exists: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email.trim().toLowerCase());
            return count != null && count > 0;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to check if email exists: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateLastLogin(String userid) {
        if (userid == null || userid.trim().isEmpty()) {
            return false;
        }
        
        try {
            String sql = "UPDATE users SET last_login = ? WHERE uid = ?";
            int rowsAffected = jdbcTemplate.update(sql, getCurrentISTTimestamp(), userid.trim());
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to update last login: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updatePassword(PasswordResetRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Password reset request cannot be null");
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }
        
        try {
            String sql = "UPDATE users SET password = ? WHERE email = ?";
            int rowsAffected = jdbcTemplate.update(sql, 
                request.getNewPassword(), 
                request.getEmail().trim().toLowerCase());
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to update password: " + e.getMessage(), e);
        }
    }

    @Override
    public String getEmailByUserId(String userId) {
        try {
            String sql = "SELECT email FROM users WHERE uid = ?";
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public String getNameByUserId(String userId) {
        try {
            String sql = "SELECT name FROM users WHERE uid = ?";
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to get user name: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateUserProfile(String email, String name, String phone) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        try {
            StringBuilder sqlBuilder = new StringBuilder("UPDATE users SET ");
            List<Object> params = new ArrayList<>();
            
            boolean hasUpdates = false;
            
            if (name != null && !name.trim().isEmpty()) {
                sqlBuilder.append("name = ?");
                params.add(name.trim());
                hasUpdates = true;
            }
            
            if (phone != null) {
                if (hasUpdates) {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("phone = ?");
                params.add(phone.trim().isEmpty() ? null : phone.trim());
                hasUpdates = true;
            }
            
            if (!hasUpdates) {
                return false; // Nothing to update
            }
            
            sqlBuilder.append(" WHERE email = ?");
            params.add(email.trim().toLowerCase());
            
            int rowsAffected = jdbcTemplate.update(sqlBuilder.toString(), params.toArray());
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to update user profile: " + e.getMessage(), e);
        }
    }
}
