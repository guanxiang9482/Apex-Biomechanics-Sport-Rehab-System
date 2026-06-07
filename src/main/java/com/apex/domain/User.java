package com.apex.domain;

import java.time.LocalDateTime;

public abstract class User {

    private int userId;
    private String username;
    private String passwordHash;
    private String email;
    private Role role;
    private boolean isActive;
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;

    // Constructor for new user creation
    public User(String username, String passwordHash,
                String email, Role role) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.email        = email;
        this.role         = role;
        this.isActive     = true;
        this.createdAt    = LocalDateTime.now();
    }

    // Constructor for loading from database
    public User(int userId, String username, String passwordHash,
                String email, Role role, boolean isActive,
                LocalDateTime lastActive, LocalDateTime createdAt) {
        this.userId       = userId;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.email        = email;
        this.role         = role;
        this.isActive     = isActive;
        this.lastActive   = lastActive;
        this.createdAt    = createdAt;
    }

    // LSP contract — all subclasses must fulfill these
    public abstract boolean login(String password);
    public abstract void resetPassword(String newPasswordHash);

    // Common getters
    public int getUserId()          { return userId; }
    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail()        { return email; }
    public Role getRole()           { return role; }
    public boolean isActive()       { return isActive; }
    public LocalDateTime getLastActive() { return lastActive; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    // Controlled setters with validation
    public void setUserId(int userId) {
        if (userId <= 0) throw new IllegalArgumentException(
            "User ID must be positive.");
        this.userId = userId;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }

    protected void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank())
            throw new IllegalArgumentException(
                "Password hash cannot be empty.");
        this.passwordHash = passwordHash;
    }
}
