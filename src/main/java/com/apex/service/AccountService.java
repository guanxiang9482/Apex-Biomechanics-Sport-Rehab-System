package com.apex.service;

import com.apex.domain.*;
import com.apex.repository.interfaces.UserRepository;
import com.apex.repository.interfaces.AthleteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * SRP: Responsible strictly for security and credentials.
 * Handles UC1 (Register), UC2 (Login), UC3 (Logout),
 * UC4 (Reset Password).
 */
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final AthleteRepository athleteRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository userRepository,
                          AthleteRepository athleteRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository   = userRepository;
        this.athleteRepository = athleteRepository;
        this.passwordEncoder  = passwordEncoder;
    }

    // UC1 — Register Account
    public User createAccount(String username, String password,
                              String email, String fullName,
                              Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already taken: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email already registered: " + email);
        }

        String passwordHash = passwordEncoder.encode(password);

        User user = switch (role) {
            case ATHLETE ->
                new Athlete(username, passwordHash, email, fullName);
            case THERAPIST ->
                new Physiotherapist(username, passwordHash,
                        email, fullName, "", "");
            case ADMIN ->
                new Administrator(username, passwordHash,
                        email, fullName, "General");
        };

        userRepository.save(user);

        // Retrieve generated ID
        Optional<User> saved = userRepository.findByUsername(username);
        saved.ifPresent(u -> user.setUserId(u.getUserId()));

        return user;
    }

    // UC2 — Login / Authenticate
    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();
        if (!user.isActive()) return Optional.empty();

        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            userRepository.updateLastActive(user.getUserId());
            return Optional.of(user);
        }
        return Optional.empty();
    }

    // UC4 — Reset Password
    public boolean resetPassword(String username,
                                 String email,
                                 String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        if (!user.getEmail().equals(email)) return false;

        String newHash = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(user.getUserId(), newHash);
        return true;
    }

    // UC19 — Deactivate account
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
