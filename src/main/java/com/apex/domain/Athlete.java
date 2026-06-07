package com.apex.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Athlete extends User {

    private String fullName;
    private LocalDate dateOfBirth;
    private String phone;
    private String injuryStatus;
    private double bodyWeightKg;
    private double heightCm;
    private String postureNotes;

    // Constructor for new athlete registration
    public Athlete(String username, String passwordHash,
                   String email, String fullName) {
        super(username, passwordHash, email, Role.ATHLETE);
        this.fullName      = fullName;
        this.injuryStatus  = "None";
    }

    // Constructor for loading from database
    public Athlete(int userId, String username, String passwordHash,
                   String email, boolean isActive,
                   LocalDateTime lastActive, LocalDateTime createdAt,
                   String fullName, LocalDate dateOfBirth, String phone,
                   String injuryStatus, double bodyWeightKg,
                   double heightCm, String postureNotes) {
        super(userId, username, passwordHash, email, Role.ATHLETE,
              isActive, lastActive, createdAt);
        this.fullName      = fullName;
        this.dateOfBirth   = dateOfBirth;
        this.phone         = phone;
        this.injuryStatus  = injuryStatus;
        this.bodyWeightKg  = bodyWeightKg;
        this.heightCm      = heightCm;
        this.postureNotes  = postureNotes;
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
    public String getFullName()      { return fullName; }
    public LocalDate getDateOfBirth(){ return dateOfBirth; }
    public String getPhone()         { return phone; }
    public String getInjuryStatus()  { return injuryStatus; }
    public double getBodyWeightKg()  { return bodyWeightKg; }
    public double getHeightCm()      { return heightCm; }
    public String getPostureNotes()  { return postureNotes; }

    // Controlled setters with validation
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException(
                "Full name cannot be empty.");
        this.fullName = fullName;
    }

    public void setBodyWeightKg(double bodyWeightKg) {
        if (bodyWeightKg <= 0) throw new IllegalArgumentException(
            "Body weight must be positive.");
        this.bodyWeightKg = bodyWeightKg;
    }

    public void setHeightCm(double heightCm) {
        if (heightCm <= 0) throw new IllegalArgumentException(
            "Height must be positive.");
        this.heightCm = heightCm;
    }

    public void setPhone(String phone)             { this.phone = phone; }
    public void setInjuryStatus(String status)     { this.injuryStatus = status; }
    public void setDateOfBirth(LocalDate dob)      { this.dateOfBirth = dob; }
    public void setPostureNotes(String notes)      { this.postureNotes = notes; }
}
