package com.apex.domain;

import java.time.LocalDateTime;

public class Session {

    private int sessionId;
    private int athleteId;
    private int therapistId;
    private int facilityId;
    private LocalDateTime sessionDate;
    private int durationMins;
    private String sessionType;
    private SessionStatus status;
    private String notes;
    private LocalDateTime createdAt;

    // Constructor for new session booking
    public Session(int athleteId, int therapistId, int facilityId,
                   LocalDateTime sessionDate, int durationMins,
                   String sessionType) {
        this.athleteId   = athleteId;
        this.therapistId = therapistId;
        this.facilityId  = facilityId;
        this.sessionDate = sessionDate;
        this.durationMins = durationMins;
        this.sessionType = sessionType;
        this.status      = SessionStatus.SCHEDULED;
        this.createdAt   = LocalDateTime.now();
    }

    // Constructor for loading from database
    public Session(int sessionId, int athleteId, int therapistId,
                   int facilityId, LocalDateTime sessionDate,
                   int durationMins, String sessionType,
                   SessionStatus status, String notes,
                   LocalDateTime createdAt) {
        this.sessionId    = sessionId;
        this.athleteId    = athleteId;
        this.therapistId  = therapistId;
        this.facilityId   = facilityId;
        this.sessionDate  = sessionDate;
        this.durationMins = durationMins;
        this.sessionType  = sessionType;
        this.status       = status;
        this.notes        = notes;
        this.createdAt    = createdAt;
    }

    // Encapsulated status transition — gatekeeper method
    public void updateStatus(SessionStatus newStatus) {
        if (this.status == SessionStatus.CANCELLED) {
            throw new IllegalStateException(
                "Cannot update a cancelled session.");
        }
        this.status = newStatus;
    }

    // Getters
    public int getSessionId()            { return sessionId; }
    public int getAthleteId()            { return athleteId; }
    public int getTherapistId()          { return therapistId; }
    public int getFacilityId()           { return facilityId; }
    public LocalDateTime getSessionDate(){ return sessionDate; }
    public int getDurationMins()         { return durationMins; }
    public double getDuration()          { return durationMins; }
    public String getSessionType()       { return sessionType; }
    public SessionStatus getStatus()     { return status; }
    public String getNotes()             { return notes; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    // Setters
    public void setSessionId(int sessionId)          { this.sessionId = sessionId; }
    public void setNotes(String notes)               { this.notes = notes; }
    public void setSessionDate(LocalDateTime date)   { this.sessionDate = date; }
    public void setTherapistId(int therapistId)      { this.therapistId = therapistId; }
    public void setFacilityId(int facilityId)        { this.facilityId = facilityId; }
}
