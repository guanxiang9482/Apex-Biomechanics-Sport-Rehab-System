package com.apex.domain;

import java.time.LocalDateTime;

public class Physiotherapist extends User {

    private String fullName;
    private String specialization;
    private String phone;
    private String licenseNumber;

    // Constructor for new therapist creation
    public Physiotherapist(String username, String passwordHash,
                           String email, String fullName,
                           String specialization, String licenseNumber) {
        super(username, passwordHash, email, Role.THERAPIST);
        this.fullName       = fullName;
        this.specialization = specialization;
        this.licenseNumber  = licenseNumber;
    }

    // Constructor for loading from database
    public Physiotherapist(int userId, String username, String passwordHash,
                           String email, boolean isActive,
                           LocalDateTime lastActive, LocalDateTime createdAt,
                           String fullName, String specialization,
                           String phone, String licenseNumber) {
        super(userId, username, passwordHash, email, Role.THERAPIST,
              isActive, lastActive, createdAt);
        this.fullName       = fullName;
        this.specialization = specialization;
        this.phone          = phone;
        this.licenseNumber  = licenseNumber;
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
    public String getFullName()       { return fullName; }
    public String getSpecialization() { return specialization; }
    public String getPhone()          { return phone; }
    public String getLicenseNumber()  { return licenseNumber; }

    // Setters
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException(
                "Full name cannot be empty.");
        this.fullName = fullName;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setPhone(String phone) { this.phone = phone; }
}
