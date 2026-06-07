package com.apex.domain;

import java.time.LocalDateTime;

public class BiomechanicalRecord {

    private int recordId;
    private int sessionId;
    private double jumpPower;
    private double jointMobility;
    private double postureScore;
    private String notes;
    private LocalDateTime recordedAt;

    // Constructor for new record entry
    public BiomechanicalRecord(int sessionId, double jumpPower,
                               double jointMobility, double postureScore,
                               String notes) {
        this.sessionId     = sessionId;
        this.jumpPower     = validatePositive(jumpPower, "Jump power");
        this.jointMobility = validatePositive(jointMobility, "Joint mobility");
        this.postureScore  = validatePositive(postureScore, "Posture score");
        this.notes         = notes;
        this.recordedAt    = LocalDateTime.now();
    }

    // Constructor for loading from database
    public BiomechanicalRecord(int recordId, int sessionId,
                               double jumpPower, double jointMobility,
                               double postureScore, String notes,
                               LocalDateTime recordedAt) {
        this.recordId      = recordId;
        this.sessionId     = sessionId;
        this.jumpPower     = jumpPower;
        this.jointMobility = jointMobility;
        this.postureScore  = postureScore;
        this.notes         = notes;
        this.recordedAt    = recordedAt;
    }

    // Validation gatekeeper — encapsulation in action
    private double validatePositive(double value, String fieldName) {
        if (value < 0) throw new IllegalArgumentException(
            fieldName + " cannot be negative.");
        return value;
    }

    // Getters
    public int getRecordId()              { return recordId; }
    public int getSessionId()             { return sessionId; }
    public double getJumpPower()          { return jumpPower; }
    public double getJointMobility()      { return jointMobility; }
    public double getPostureScore()       { return postureScore; }
    public String getNotes()              { return notes; }
    public LocalDateTime getRecordedAt()  { return recordedAt; }

    // Validated setters
    public void setRecordId(int recordId)         { this.recordId = recordId; }
    public void setNotes(String notes)            { this.notes = notes; }
    public void setRecordedAt(LocalDateTime time) { this.recordedAt = time; }

    public void setJumpPower(double jumpPower) {
        this.jumpPower = validatePositive(jumpPower, "Jump power");
    }

    public void setJointMobility(double jointMobility) {
        this.jointMobility = validatePositive(jointMobility, "Joint mobility");
    }

    public void setPostureScore(double postureScore) {
        this.postureScore = validatePositive(postureScore, "Posture score");
    }
}
