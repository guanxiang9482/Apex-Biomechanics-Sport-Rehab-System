package com.apex.service;

import com.apex.domain.*;
import com.apex.repository.interfaces.AthleteRepository;
import com.apex.repository.interfaces.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * SRP: Responsible strictly for authentication
 * and account lifecycle management.
 * UC1: Register (Athlete only via public endpoint)
 * UC2: Login, UC3: Logout, UC4: Reset Password
 * UC19: Add Staff (Therapist/Admin via Admin only)
 */
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final AthleteRepository athleteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;

    public AccountService(UserRepository userRepository,
                          AthleteRepository athleteRepository,
                          PasswordEncoder passwordEncoder,
                          JdbcTemplate jdbc) {
        this.userRepository   = userRepository;
        this.athleteRepository = athleteRepository;
        this.passwordEncoder  = passwordEncoder;
        this.jdbc             = jdbc;
    }

    /**
     * Creates user account + role-specific record atomically.
     *
     * Called by:
     * - AuthController.register() with Role.ATHLETE (public)
     * - AdminController.addStaff() with any role (admin only)
     *
     * Security note: Role escalation is prevented at the
     * controller layer — this service trusts its caller.
     */
    public User createAccount(String username, String password,
                              String email, String fullname,
                              String contact, Role role) {
        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException(
                    "Username already taken: " + username);
        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException(
                    "Email already registered: " + email);

        String hash = passwordEncoder.encode(password);

        User user = switch (role) {
            case ATHLETE -> new Athlete(username, hash, email,
                    fullname, contact, null, null, 0, 0);
            case THERAPIST -> new Physiotherapist(username, hash,
                    email, fullname, contact, "", "");
            case ADMIN -> new Administrator(username, hash,
                    email, fullname, contact, "General");
        };

        // Step 1 — Insert into users table
        userRepository.save(user);

        // Step 2 — Retrieve generated user_id
        Optional<User> saved =
                userRepository.findByUsername(username);
        saved.ifPresent(u -> user.setUserId(u.getUserId()));

        if (user.getUserId() <= 0)
            throw new RuntimeException(
                    "Failed to retrieve generated user ID");

        // Step 3 — Insert role-specific record
        // Each role has its own table per ERD
        try {
            switch (role) {
                case ATHLETE -> athleteRepository.save(
                        (Athlete) user);
                case THERAPIST -> jdbc.update(
                        "INSERT INTO physiotherapists " +
                        "(user_id, specialization, license_number) " +
                        "VALUES (?, ?, ?)",
                        user.getUserId(), "", "");
                case ADMIN -> jdbc.update(
                        "INSERT INTO administrators " +
                        "(user_id, department) VALUES (?, ?)",
                        user.getUserId(), "General");
            }
        } catch (Exception e) {
            // Rollback user if role-specific insert fails
            userRepository.delete(user.getUserId());
            throw new RuntimeException(
                    "Failed to create role profile: "
                    + e.getMessage(), e);
        }

        return user;
    }

    // UC2 — Authenticate / Login
    public Optional<User> authenticate(String username,
                                       String password) {
        Optional<User> userOpt =
                userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();
        if (!user.isActive()) return Optional.empty();

        if (passwordEncoder.matches(password, user.getPassword())) {
            userRepository.updateLastLoginAt(user.getUserId());
            return Optional.of(user);
        }
        return Optional.empty();
    }

    // UC4 — Reset Password
    public boolean resetPassword(String username, String email,
                                 String newPassword) {
        Optional<User> userOpt =
                userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return false;
        User user = userOpt.get();
        if (!user.getEmail().equals(email)) return false;
        userRepository.updatePassword(user.getUserId(),
                passwordEncoder.encode(newPassword));
        return true;
    }

    // UC19 — Deactivate Account
    public void deactivateAccount(int userId) {
        userRepository.setActiveStatus(userId, false);
    }

    public void deleteAccount(int userId) {
        userRepository.delete(userId);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
