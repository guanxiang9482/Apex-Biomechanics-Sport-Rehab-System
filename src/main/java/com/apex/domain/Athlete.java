package com.apex.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Athlete extends User {

    private int athleteId;
    private LocalDate dateOfBirth;
    private String injuryStatus;
    private String sport;
    private double bodyWeightKg;
    private double heightCm;

    // Constructor for new athlete registration
    public Athlete(String username, String password,
                   String email, String fullName, String contact, String sport, 
                   LocalDate dateOfBirth, double bodyWeightKg, double heightCm) {
        super(username, password, email, Role.ATHLETE, fullName, contact);
        this.sport = sport;
        this.dateOfBirth = dateOfBirth;
        this.injuryStatus = "None";
        this.bodyWeightKg = bodyWeightKg;
        this.heightCm = heightCm;
    }

    // Constructor for loading from database
    public Athlete(int userId, String username, String password,
                   String email, String fullname, String contact, boolean isActive,
                   LocalDateTime lastActive, LocalDateTime createdAt,
                   LocalDate dateOfBirth,
                   String injuryStatus, double bodyWeightKg,
                   double heightCm, String sport, int athleteId) {
        super(userId, username, password, email, Role.ATHLETE, fullname, contact,
              lastActive, createdAt, isActive);
        this.dateOfBirth   = dateOfBirth;
        this.injuryStatus  = injuryStatus;
        this.bodyWeightKg  = bodyWeightKg;
        this.heightCm      = heightCm;
        this.sport         = sport;
        this.athleteId      = athleteId; // Simple ID generation strategy
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
    public LocalDate getDateOfBirth(){ return dateOfBirth; }
    public String getInjuryStatus()  { return injuryStatus; }
    public double getBodyWeightKg()  { return bodyWeightKg; }
    public double getHeightCm()      { return heightCm; }
    public String getSport()         { return (sport == null) ? "" :sport; }
    public int getAthleteId()     { return athleteId; }
    public double getBmi() {
        if (heightCm <= 0 || bodyWeightKg <= 0) return 0.0;
        double heightM = heightCm / 100.0;
        return Math.round((bodyWeightKg / (heightM * heightM)) * 100.0) / 100.0;
    }

    // Controlled setters with validation
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

    public void setInjuryStatus(String status)     { this.injuryStatus = status; }
    public void setDateOfBirth(LocalDate dob)      { this.dateOfBirth = dob; }
    public void setSport(String sport)             { this.sport = sport; }
    public void setAthleteId(int athleteId) {
        if (athleteId <= 0) throw new IllegalArgumentException(
            "Athlete ID must be positive.");
        this.athleteId = athleteId;
    }
}
