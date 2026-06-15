package com.apex.domain;

import java.time.LocalDateTime;

public class Physiotherapist extends User {

    private int therapistId;
    private String specialization;
    private String licenseNumber;

    // Constructor for new therapist creation
    public Physiotherapist(String username, String password,
                           String email, String fullname, String contact,
                           String specialization, String licenseNumber) {
        super(username, password, email, Role.THERAPIST, fullname, contact);
        this.specialization = specialization;
        this.licenseNumber  = licenseNumber;
    }

    // Constructor for loading from database
    public Physiotherapist(int userId, String username, String password,
                           String email, boolean isActive,
                           LocalDateTime lastActive, LocalDateTime createdAt,
                           String fullname, String specialization,
                           String contact, String licenseNumber, int therapistId) {
        super(userId, username, password, email, Role.THERAPIST,
              fullname, contact, lastActive, createdAt, isActive);
        this.specialization = specialization;
        this.licenseNumber  = licenseNumber;
        this.therapistId = therapistId;
    }

    // LSP contract fulfillment
    @Override
    public boolean login(String password) {
        return this.isActive() &&
               this.getPassword().equals(password);
    }

    @Override
    public void resetPassword(String newpassword) {
        setPassword(newpassword);
    }

    // Getters
    public String getSpecialization() { return specialization; }
    public String getLicenseNumber()  { return licenseNumber; }
    public int getTherapistId() { return therapistId; }

    // Setters
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    public void setTherapistId(int therapistId) {
        if (therapistId <= 0) throw new IllegalArgumentException(
            "Therapist ID must be positive.");
        this.therapistId = therapistId;
    }
}
