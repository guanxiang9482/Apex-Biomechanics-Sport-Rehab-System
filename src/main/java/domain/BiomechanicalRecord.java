package main.java.domain;

import java.time.LocalDateTime;

public class BiomechanicalRecord {
    private int recordID;
    private int SessionID;
    private double jumpPower;
    private double jointMobility;
    private double postureScore;
    private LocalDateTime recordedAt;

    public BiomechanicalRecord(int recordID, int SessionID, double jumpPower, double jointMobility, double postureScore, LocalDateTime recordedAt) {
        this.recordID = recordID;
        this.SessionID = SessionID;
        this.jumpPower = jumpPower;
        this.jointMobility = jointMobility;
        this.postureScore = postureScore;
        this.recordedAt = recordedAt;
    }

    public int getRecordID() {
        return recordID;
    }
    public int getSessionID() {
        return SessionID;
    }
    public double getJumpPower() {
        return jumpPower;
    }
    public double getJointMobility() {
        return jointMobility;
    }
    public double getPostureScore() {
        return postureScore;
    }
    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }
    public void setJumpPower(double jumpPower) {
        this.jumpPower = jumpPower;
    }
    public void setJointMobility(double jointMobility) {
        this.jointMobility = jointMobility;
    }
    public void setPostureScore(double postureScore) {
        this.postureScore = postureScore;
    }
    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
