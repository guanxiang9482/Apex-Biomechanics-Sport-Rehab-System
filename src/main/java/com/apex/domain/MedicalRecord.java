package com.apex.domain;

import java.time.LocalDateTime;

public class MedicalRecord {
    private int recordId;
    private int athleteId;
    private int createdByTherapist;
    private LocalDateTime createdAt;
    private String diagnosisNotes;
    private LocalDateTime updatedAt;

    // Constructor for new record
    public MedicalRecord(int athleteId, int createdByTherapist,
                         String diagnosisNotes) {
        this.athleteId          = athleteId;
        this.createdByTherapist = createdByTherapist;
        this.diagnosisNotes     = diagnosisNotes;
        this.createdAt          = LocalDateTime.now();
        this.updatedAt          = LocalDateTime.now();
    }

    // Constructor for loading from database
    public MedicalRecord(int recordId, int athleteId,
                         int createdByTherapist,
                         LocalDateTime createdAt,
                         String diagnosisNotes,
                         LocalDateTime updatedAt) {
        this.recordId           = recordId;
        this.athleteId          = athleteId;
        this.createdByTherapist = createdByTherapist;
        this.createdAt          = createdAt;
        this.diagnosisNotes     = diagnosisNotes;
        this.updatedAt          = updatedAt;
    }

    public int getRecordId()              { return recordId; }
    public int getAthleteId()             { return athleteId; }
    public int getCreatedByTherapist()    { return createdByTherapist; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public String getDiagnosisNotes()     { return diagnosisNotes; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }

    public void setRecordId(int id)       { this.recordId = id; }
    public void setDiagnosisNotes(String n){ this.diagnosisNotes = n; }
    public void setUpdatedAt(LocalDateTime t){ this.updatedAt = t; }
}
