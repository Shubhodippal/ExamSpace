package com.example.demo.User;

public interface UserDao {
    int saveUser(User user);
    User getUserByEmail(String email);
    boolean emailExists(String email);
    boolean isUserIdExists(String userid);
    boolean updatePassword(PasswordResetRequest request);
    boolean updateLastLogin(String email);
    String getEmailByUserId(String userId);
    String getNameByUserId(String userId); // Newly added method
    boolean updateUserProfile(String email, String name, String phone);
}
