package com.apex.controller;

import com.apex.domain.Role;
import com.apex.domain.User;
import com.apex.service.AccountService;
import com.apex.service.observer.AdminObserver;
import com.apex.service.observer.AthleteObserver;
import com.apex.service.observer.NotificationEngine;
import com.apex.service.observer.TherapistObserver;
import com.apex.repository.interfaces.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Handler Tier — Authentication endpoints
 * Public routes, no RBAC restriction.
 * UC1: Register, UC2: Login, UC3: Logout, UC4: Reset Password
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;
    private final NotificationEngine notificationEngine;
    private final UserRepository userRepository;

    public AuthController(AccountService accountService,
                          NotificationEngine notificationEngine,
                          UserRepository userRepository) {
        this.accountService      = accountService;
        this.notificationEngine  = notificationEngine;
        this.userRepository      = userRepository;
    }

    // UC1 — Register Account (Athlete self-registration)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            String email    = body.get("email");
            String fullName = body.get("fullName");

            User user = accountService.createAccount(
                    username, password, email, fullName, Role.ATHLETE);

            // Register new athlete as Observer for notifications
            notificationEngine.subscribe(
                    new AthleteObserver(user.getUserId(), userRepository));

            return ResponseEntity.ok(Map.of(
                    "message", "Account created successfully",
                    "userId",  user.getUserId(),
                    "role",    user.getRole().name()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC2 — Login / Authenticate
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<User> userOpt =
                accountService.authenticate(username, password);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error",
                            "Invalid credentials or account disabled"));
        }

        User user = userOpt.get();

        // Register user as Observer on login
        switch (user.getRole()) {
            case ATHLETE ->
                notificationEngine.subscribe(
                        new AthleteObserver(user.getUserId(),
                                userRepository));
            case THERAPIST ->
                notificationEngine.subscribe(
                        new TherapistObserver(user.getUserId()));
            case ADMIN ->
                notificationEngine.subscribe(
                        new AdminObserver(user.getUserId()));
        }

        return ResponseEntity.ok(Map.of(
                "message",  "Login successful",
                "userId",   user.getUserId(),
                "username", user.getUsername(),
                "role",     user.getRole().name()
        ));
    }

    // UC3 — Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, Object> body) {
        int userId = (Integer) body.get("userId");

        // Unsubscribe from Observer on logout
        notificationEngine.unsubscribeById(userId);

        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully"));
    }

    // UC4 — Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> body) {
        String username    = body.get("username");
        String email       = body.get("email");
        String newPassword = body.get("newPassword");

        boolean success = accountService.resetPassword(
                username, email, newPassword);

        if (!success) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Username and email do not match"));
        }
        return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully"));
    }
}
