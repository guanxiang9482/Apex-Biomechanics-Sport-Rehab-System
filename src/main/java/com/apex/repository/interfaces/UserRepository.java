package com.apex.repository.interfaces;

import java.util.Optional;

import com.apex.domain.User;

// ISP: Only auth and credential operations
public interface UserRepository {
    void save(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findById(int userId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    void updatePassword(int userId, String newPasswordHash);
    void updateLastLoginAt(int userId);
    void setActiveStatus(int userId, boolean isActive);
    void delete(int userId);
}
