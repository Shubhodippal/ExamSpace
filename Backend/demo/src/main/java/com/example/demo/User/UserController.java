package com.example.demo.User;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Security.JwtUtil;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserDao userDAO;

    @Autowired
    private JwtUtil jwtUtil;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Password validation pattern (minimum 8 characters, at least one letter and one number)
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$");

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> saveUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            String validationError = validateUserRegistration(user);
            if (validationError != null) {
                response.put("status", "error");
                response.put("message", validationError);
                return ResponseEntity.badRequest().body(response);
            }

            // Check if email already exists
            if (userDAO.emailExists(user.getEmail().trim().toLowerCase())) {
                response.put("status", "error");
                response.put("message", "Email already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Generate unique user ID with retry limit
            String userid = generateUniqueUserId();
            if (userid == null) {
                response.put("status", "error");
                response.put("message", "Failed to generate unique user ID");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            user.setUserid(userid);
            user.setEmail(user.getEmail().trim().toLowerCase());
            user.setName(user.getName().trim());
            if (user.getPhone() != null) {
                user.setPhone(user.getPhone().trim());
            }

            // Encrypt password
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encryptedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encryptedPassword);

            int rows = userDAO.saveUser(user);
            if (rows > 0) {
                response.put("status", "success");
                response.put("message", "User registered successfully");
                response.put("userid", userid);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to save user");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (loginRequest == null) {
                response.put("status", "error");
                response.put("message", "Login request cannot be null");
                return ResponseEntity.badRequest().body(response);
            }

            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Password is required");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userDAO.getUserByEmail(loginRequest.getEmail().trim().toLowerCase());

            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                response.put("status", "error");
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Update last login - use userid instead of email
            userDAO.updateLastLogin(user.getUserid());

            String accessToken = jwtUtil.generateToken(user.getUserid(), user.getEmail(), user.getName());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserid(), user.getEmail(), user.getName());
            
            response.put("status", "success");
            response.put("token", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("message", "Login successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody PasswordResetRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (request == null) {
                response.put("status", "error");
                response.put("message", "Password reset request cannot be null");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "New password is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate password strength
            if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
                response.put("status", "error");
                response.put("message", "Password must be at least 8 characters long and contain at least one letter and one number");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userDAO.getUserByEmail(request.getEmail().trim().toLowerCase());
            if (user == null) {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encryptedPassword = passwordEncoder.encode(request.getNewPassword());
            
            request.setEmail(request.getEmail().trim().toLowerCase());
            request.setNewPassword(encryptedPassword);
            
            boolean updated = userDAO.updatePassword(request);
            if (updated) {
                response.put("status", "success");
                response.put("message", "Password reset successful");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to reset password");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Password reset failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (refreshTokenRequest == null) {
                response.put("status", "error");
                response.put("message", "Refresh token request cannot be null");
                return ResponseEntity.badRequest().body(response);
            }

            String refreshToken = refreshTokenRequest.getRefreshToken();
            
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Refresh token is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                response.put("status", "error");
                response.put("message", "Invalid or expired refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String userId = jwtUtil.extractUserIdFromRefreshToken(refreshToken);
            String email = jwtUtil.extractEmailFromRefreshToken(refreshToken);
            String name = jwtUtil.extractNameFromRefreshToken(refreshToken);
            
            if (userId == null || email == null || name == null) {
                response.put("status", "error");
                response.put("message", "Invalid token data");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String newAccessToken = jwtUtil.generateToken(userId, email, name);
            
            response.put("status", "success");
            response.put("token", newAccessToken);
            response.put("message", "Token refreshed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Token refresh failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Email configuration
    @Value("${email.host:smtp.gmail.com}")
    private String emailHost;

    @Value("${email.port:587}")
    private int emailPort;

    @Value("${email.username:}")
    private String emailUsername;

    @Value("${email.password:}")
    private String emailPassword;

    @Value("${email.ssl:false}")
    private boolean useSSL;

    @PostMapping("/otp")
    public ResponseEntity<Map<String, Object>> generateOtp(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (request == null) {
                response.put("status", "error");
                response.put("message", "Request cannot be null");
                return ResponseEntity.badRequest().body(response);
            }

            String email = request.get("email");
            String type = request.get("type");
            
            if (email == null || email.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate email format
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                response.put("status", "error");
                response.put("message", "Invalid email format");
                return ResponseEntity.badRequest().body(response);
            }

            if (type == null || type.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Type is required");
                return ResponseEntity.badRequest().body(response);
            }

            email = email.trim().toLowerCase();
            type = type.trim().toLowerCase();
            
            if ("signup".equals(type)) {
                return handleSignupOtp(email, type, response);
            } else if ("reset".equals(type)) {
                return handleResetOtp(email, type, response);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid request type. Must be 'signup' or 'reset'");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "OTP generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> handleSignupOtp(String email, String type, Map<String, Object> response) {
        try {
            // Check if email already exists for signup
            if (userDAO.emailExists(email)) {
                response.put("status", "error");
                response.put("message", "Email already registered");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            return generateAndSendOtp(email, type, response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Signup OTP generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> handleResetOtp(String email, String type, Map<String, Object> response) {
        try {
            // Check if user exists for password reset
            if (!userDAO.emailExists(email)) {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            return generateAndSendOtp(email, type, response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Reset OTP generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> generateAndSendOtp(String email, String type, Map<String, Object> response) {
        try {
            // Validate email configuration
            if (emailUsername == null || emailUsername.trim().isEmpty() || 
                emailPassword == null || emailPassword.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Email service not configured");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }

            String otp = generateOtp();
            
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encodedOtp = passwordEncoder.encode(otp);
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", email);
            claims.put("encodedOtp", encodedOtp);
            claims.put("type", type);
            
            String otpToken = jwtUtil.generateTokenWithExpiration(claims, 10 * 60 * 1000); // 10 minutes
            
            sendOtpEmail(email, otp);
            
            response.put("status", "success");
            response.put("otpToken", otpToken);
            response.put("message", "OTP sent successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (MessagingException e) {
            response.put("status", "error");
            response.put("message", "Failed to send OTP email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "OTP generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    private void sendOtpEmail(String to, String otp) throws MessagingException {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email cannot be null or empty");
        }

        if (otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP cannot be null or empty");
        }

        Properties props = new Properties();
        
        if (useSSL) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.host", emailHost);
            props.put("mail.smtp.port", emailPort);
        } else {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", emailHost);
            props.put("mail.smtp.port", emailPort);
        }
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailUsername));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Your ExamSpace OTP Code");
        message.setText("Your OTP code is: " + otp + "\n\nThis code is valid for 10 minutes.");
        
        Transport.send(message);
    }

    @PostMapping("/send_mail")
    public ResponseEntity<?> sendContactEmail(@RequestBody ContactRequest contactRequest) {
        try {
            // Validate required fields
            if (contactRequest.getName() == null || contactRequest.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Name is required"));
            }
            if (contactRequest.getEmail() == null || contactRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Email is required"));
            }
            if (contactRequest.getSubject() == null || contactRequest.getSubject().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Subject is required"));
            }
            if (contactRequest.getMessage() == null || contactRequest.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Message is required"));
            }
            
            // Send the contact message to the fixed email address
            sendContactMessage(contactRequest);
            
            return ResponseEntity.ok(Collections.singletonMap("message", "Your message has been sent successfully"));
        } catch (MessagingException e) {
            // Log the exception or handle as needed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to send message: " + e.getMessage()));
        } catch (Exception e) {
            // Log the exception or handle as needed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    private void sendContactMessage(ContactRequest contact) throws MessagingException {
        Properties props = new Properties();
        
        // Configure email properties for webmail
        if (useSSL) {
            // SSL configuration
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.host", emailHost);
            props.put("mail.smtp.port", emailPort);
        } else {
            // TLS configuration
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", emailHost);
            props.put("mail.smtp.port", emailPort);
        }
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailUsername));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("shubhodippal01@gmail.com"));
        message.setSubject("Contact Form: " + contact.getSubject());
        
        // Format the message to include sender information
        String emailBody = "Name: " + contact.getName() + "\n" +
                      "Email: " + contact.getEmail() + "\n\n" +
                      "Message:\n" + contact.getMessage();
        
        message.setText(emailBody);
        
        Transport.send(message);
    }

    private String validateUserRegistration(User user) {
        if (user == null) {
            return "User data cannot be null";
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return "Email is required";
        }

        if (!EMAIL_PATTERN.matcher(user.getEmail().trim()).matches()) {
            return "Invalid email format";
        }

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return "Name is required";
        }

        if (user.getName().trim().length() < 2) {
            return "Name must be at least 2 characters long";
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return "Password is required";
        }

        if (!PASSWORD_PATTERN.matcher(user.getPassword()).matches()) {
            return "Password must be at least 8 characters long and contain at least one letter and one number";
        }

        // Validate phone if provided
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            String phone = user.getPhone().trim().replaceAll("[\\s\\-\\(\\)]", "");
            if (phone.length() < 10 || phone.length() > 15 || !phone.matches("\\d+")) {
                return "Invalid phone number format";
            }
        }

        return null; // No validation errors
    }

    private String generateUniqueUserId() {
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            String userid = UUID.randomUUID().toString();
            if (!userDAO.isUserIdExists(userid)) {
                return userid;
            }
        }
        return null; // Failed to generate unique ID after max retries
    }

    @PutMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (request == null) {
                response.put("status", "error");
                response.put("message", "Request cannot be null");
                return ResponseEntity.badRequest().body(response);
            }

            String email = request.get("email");
            String name = request.get("name");
            String phone = request.get("phone");
            
            if (email == null || email.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate email format
            if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
                response.put("status", "error");
                response.put("message", "Invalid email format");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if at least one field to update is provided
            if ((name == null || name.trim().isEmpty()) && (phone == null || phone.trim().isEmpty())) {
                response.put("status", "error");
                response.put("message", "At least one field (name or phone) must be provided for update");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate name if provided
            if (name != null && !name.trim().isEmpty() && name.trim().length() < 2) {
                response.put("status", "error");
                response.put("message", "Name must be at least 2 characters long");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate phone if provided
            if (phone != null && !phone.trim().isEmpty()) {
                String phoneFormatted = phone.trim().replaceAll("[\\s\\-\\(\\)]", "");
                if (phoneFormatted.length() < 10 || phoneFormatted.length() > 15 || !phoneFormatted.matches("\\d+")) {
                    response.put("status", "error");
                    response.put("message", "Invalid phone number format");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Check if user exists
            User user = userDAO.getUserByEmail(email.trim().toLowerCase());
            if (user == null) {
                response.put("status", "error");
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Update the user profile
            boolean updated = userDAO.updateUserProfile(email.trim().toLowerCase(), name, phone);
            
            if (updated) {
                response.put("status", "success");
                response.put("message", "Profile updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to update profile");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Profile update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}