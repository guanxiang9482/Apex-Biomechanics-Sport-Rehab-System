package com.apex.domain;

import java.time.LocalDateTime;

public class Administrator extends User {

    private String fullName;
    private String phone;
    private String department;

    // Constructor for new admin creation
    public Administrator(String username, String passwordHash,
                         String email, String fullName,
                         String department) {
        super(username, passwordHash, email, Role.ADMIN);
        this.fullName   = fullName;
        this.department = department;
    }

    // Constructor for loading from database
    public Administrator(int userId, String username, String passwordHash,
                         String email, boolean isActive,
                         LocalDateTime lastActive, LocalDateTime createdAt,
                         String fullName, String phone, String department) {
        super(userId, username, passwordHash, email, Role.ADMIN,
              isActive, lastActive, createdAt);
        this.fullName   = fullName;
        this.phone      = phone;
        this.department = department;
    }

    // LSP contract fulfillment
    @Override
    public boolean login(String password) {
        return this.isActive() &&
               this.getPasswordHash().equals(password);
    }

    @Override
    public void resetPassword(String newPasswordHash) {
        setPasswordHash(newPasswordHash);
    }

    // Getters
    public String getFullName()   { return fullName; }
    public String getPhone()      { return phone; }
    public String getDepartment() { return department; }

    // Setters
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException(
                "Full name cannot be empty.");
        this.fullName = fullName;
    }

    public void setPhone(String phone)           { this.phone = phone; }
    public void setDepartment(String department) { this.department = department; }
}
