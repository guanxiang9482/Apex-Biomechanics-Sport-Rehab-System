package com.apex.controller;

import com.apex.domain.*;
import com.apex.repository.interfaces.UserRepository;
import com.apex.service.AccountService;
import com.apex.service.ProfileService;
import com.apex.service.observer.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * UC1: Register, UC2: Login,
 * UC3: Logout, UC4: Reset Password
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;
    private final NotificationEngine notificationEngine;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    public AuthController(AccountService accountService,
                          NotificationEngine notificationEngine,
                          UserRepository userRepository,
                          ProfileService profileService) {
        this.accountService     = accountService;
        this.notificationEngine = notificationEngine;
        this.userRepository     = userRepository;
        this.profileService     = profileService;
    }

    // UC1 — Self-registration (Athletes only)
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            String email    = body.get("email");
            String fullname = body.get("fullName");
            String contact  = body.getOrDefault("contact", "");

            User user = accountService.createAccount(
                    username, password, email,
                    fullname, contact, Role.ATHLETE);

            profileService.getProfileByUserId(user.getUserId())
                    .ifPresent(athlete -> {
                        applyOptionalPositiveDouble(body.get("bodyWeightKg"),
                                athlete::setBodyWeightKg);
                        applyOptionalPositiveDouble(body.get("heightCm"),
                                athlete::setHeightCm);
                        profileService.updateProfile(athlete);
                    });

            notificationEngine.subscribe(
                    new AthleteObserver(user.getUserId(),
                            userRepository));

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

    // UC2 — Login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<User> userOpt =
                accountService.authenticate(username, password);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of(
                    "error",
                    "Invalid credentials or account disabled"));
        }

        User user = userOpt.get();

        // Register as Observer on login — UC21
        switch (user.getRole()) {
            case ATHLETE -> notificationEngine.subscribe(
                    new AthleteObserver(user.getUserId(),
                            userRepository));
            case THERAPIST -> notificationEngine.subscribe(
                    new TherapistObserver(user.getUserId()));
            case ADMIN -> notificationEngine.subscribe(
                    new AdminObserver(user.getUserId()));
        }

        return ResponseEntity.ok(Map.of(
                "message",  "Login successful",
                "userId",   user.getUserId(),
                "username", user.getUsername(),
                "role",     user.getRole().name(),
                "fullname", user.getFullname()
        ));
    }

    // UC3 — Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestBody Map<String, Object> body) {
        int userId = (Integer) body.get("userId");
        notificationEngine.unsubscribeById(userId);
        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully"));
    }

    // UC4 — Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> body) {
        boolean success = accountService.resetPassword(
                body.get("username"),
                body.get("email"),
                body.get("newPassword"));

        if (!success) return ResponseEntity.badRequest()
                .body(Map.of("error",
                        "Username and email do not match"));

        return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully"));
    }

    private void applyOptionalPositiveDouble(String value,
                                             java.util.function.DoubleConsumer setter) {
        if (value == null || value.isBlank()) return;
        double parsed = Double.parseDouble(value);
        if (parsed > 0) setter.accept(parsed);
    }
}
