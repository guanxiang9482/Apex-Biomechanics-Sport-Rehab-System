package com.apex.domain;

import java.time.LocalDateTime;

public class ClinicalReport {

    private int reportId;
    private int athleteId;
    private int therapistId;
    private LocalDateTime reportDate;
    private String summary;
    private ReportStatus status;

    // Constructor for new report
    public ClinicalReport(int athleteId, int therapistId, String summary) {
        this.athleteId   = athleteId;
        this.therapistId = therapistId;
        this.summary     = summary;
        this.status      = ReportStatus.DRAFT;
        this.reportDate  = LocalDateTime.now();
    }

    // Constructor for loading from database
    public ClinicalReport(int reportId, int athleteId, int therapistId,
                          LocalDateTime reportDate, String summary,
                          ReportStatus status) {
        this.reportId    = reportId;
        this.athleteId   = athleteId;
        this.therapistId = therapistId;
        this.reportDate  = reportDate;
        this.summary     = summary;
        this.status      = status;
    }

    public void finalizeReport() {
        if (this.status == ReportStatus.FINALIZED)
            throw new IllegalStateException("Report is already finalized.");
        this.status = ReportStatus.FINALIZED;
    }

    // Getters
    public int getReportId()              { return reportId; }
    public int getAthleteId()             { return athleteId; }
    public int getTherapistId()           { return therapistId; }
    public LocalDateTime getReportDate()  { return reportDate; }
    public String getSummary()            { return summary; }
    public ReportStatus getStatus()       { return status; }

    // Setters
    public void setReportId(int reportId)   { this.reportId = reportId; }
    public void setSummary(String summary)  { this.summary = summary; }
    public void setStatus(ReportStatus status) { this.status = status; }
}
