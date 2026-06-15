package com.apex.domain;

import java.time.LocalDateTime;

public abstract class User {

    private int userId;
    private String username;
    private String password;
    private String email;
    private Role role;
    private String fullname;
    private boolean isActive;
    private String contact;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    // Constructor for new user creation
    public User(String username, String password,
                String email, Role role, String fullname, String contact) {
        this.username  = username;
        this.password  = password;
        this.email     = email;
        this.role      = role;
        this.fullname  = fullname;
        this.contact   = contact;
        this.isActive  = true;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for loading from database
    public User(int userId, String username, String password,
                String email, Role role, String fullname, String contact,
                LocalDateTime lastLoginAt, LocalDateTime createdAt,
                boolean isActive) {
        this.userId      = userId;
        this.username    = username;
        this.password    = password;
        this.email       = email;
        this.role        = role;
        this.fullname    = fullname;
        this.contact     = contact;
        this.lastLoginAt = lastLoginAt;
        this.createdAt   = createdAt;
        this.isActive    = isActive;
    }

    // LSP contract — all subclasses must fulfill these
    public abstract boolean login(String password);
    public abstract void resetPassword(String newpassword);

    // Common getters
    public int getUserId()          { return userId; }
    public String getUsername()     { return username; }
    public String getPassword() { return password; }
    public String getEmail()        { return email; }
    public Role getRole()           { return role; }
    public String getFullname()     { return fullname; }
    public String getContact()      { return contact; }
    public boolean isActive()       { return isActive; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    // Controlled setters with validation
    public void setUserId(int userId) {
        if (userId <= 0) throw new IllegalArgumentException(
            "User ID must be positive.");
        this.userId = userId;
    }
    public void setUsername(String username) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException(
                "Username cannot be empty.");
        this.username = username;
    }
    public void setEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@"))
            throw new IllegalArgumentException(
                "Invalid email address.");
        this.email = email;
    }

    public void setContact(String contact) {
        if (contact == null || contact.isBlank())
            throw new IllegalArgumentException(
                "Contact information cannot be empty.");
        this.contact = contact;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void setPassword(String password) {
        if (password == null || password.isBlank())
            throw new IllegalArgumentException(
                "Password cannot be empty. Please enter a valid password.");
        this.password = password;
    }
}
