package com.apex.domain;

import java.time.LocalDateTime;

public class BiomechanicalRecord {

    private int recordId;
    private int athleteId;     
    private int therapistId;
    private int sessionId;
    private double jumpPower;
    private double jointMobility;
    private double postureScore;
    private String treatmentNote;
    private LocalDateTime recordedAt;

    // Constructor for new record entry
    public BiomechanicalRecord(int athleteId, int therapistId,
                               int sessionId, double jumpPower,
                               double jointMobility,
                               double postureScore,
                               String treatmentNote) {
        this.athleteId    = athleteId;
        this.therapistId  = therapistId;
        this.sessionId    = sessionId;
        this.jumpPower    = validate(jumpPower,    "Jump power");
        this.jointMobility= validate(jointMobility,"Joint mobility");
        this.postureScore = validate(postureScore, "Posture score");
        this.treatmentNote= treatmentNote;
        this.recordedAt   = LocalDateTime.now();
    }

    // Constructor for loading from database
    public BiomechanicalRecord(int recordId, int athleteId,
                               int therapistId, int sessionId,
                               double jumpPower, double jointMobility,
                               double postureScore,
                               LocalDateTime recordedAt,
                               String treatmentNote) {
        this.recordId     = recordId;
        this.athleteId    = athleteId;
        this.therapistId  = therapistId;
        this.sessionId    = sessionId;
        this.jumpPower    = jumpPower;
        this.jointMobility= jointMobility;
        this.postureScore = postureScore;
        this.recordedAt   = recordedAt;
        this.treatmentNote= treatmentNote;
    }

    // Validation gatekeeper — encapsulation in action
    private double validate(double value, String field) {
        if (value < 0) throw new IllegalArgumentException(
                field + " cannot be negative.");
        return value;
    }

    // Getters
    public int getRecordId()              { return recordId; }
    public int getAthleteId()             { return athleteId; }
    public int getTherapistId()           { return therapistId; }
    public int getSessionId()             { return sessionId; }
    public double getJumpPower()          { return jumpPower; }
    public double getJointMobility()      { return jointMobility; }
    public double getPostureScore()       { return postureScore; }
    public LocalDateTime getRecordedAt()  { return recordedAt; }
    public String getTreatmentNote()      { return treatmentNote; }

    public void setRecordId(int id)       { this.recordId = id; }
    public void setTreatmentNote(String n){ this.treatmentNote = n; }
    public void setRecordedAt(LocalDateTime t){ this.recordedAt = t; }

    public void setJumpPower(double v) {
        this.jumpPower = validate(v, "Jump power");
    }
    public void setJointMobility(double v) {
        this.jointMobility = validate(v, "Joint mobility");
    }
    public void setPostureScore(double v) {
        this.postureScore = validate(v, "Posture score");
    }
}
