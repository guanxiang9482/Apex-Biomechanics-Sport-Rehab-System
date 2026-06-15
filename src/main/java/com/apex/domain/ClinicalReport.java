package com.apex.domain;

import java.time.LocalDateTime;

public class ClinicalReport {

    private int reportId;
    private int submitByTherapist;
    private Integer approveByAdmin;
    private String reportType;
    private String description;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private ReportStatus status;

    // Constructor for new report
    public ClinicalReport(int submitByTherapist,
                          String reportType, String description) {
        this.submitByTherapist = submitByTherapist;
        this.reportType        = reportType;
        this.description       = description;
        this.submittedAt       = LocalDateTime.now();
        this.status            = ReportStatus.DRAFT;
    }

    // Constructor for loading from database
    public ClinicalReport(int reportId, int submitByTherapist,
                          Integer approveByAdmin, String reportType,
                          String description,
                          LocalDateTime submittedAt,
                          LocalDateTime reviewedAt,
                          ReportStatus status) {
        this.reportId          = reportId;
        this.submitByTherapist = submitByTherapist;
        this.approveByAdmin    = approveByAdmin;
        this.reportType        = reportType;
        this.description       = description;
        this.submittedAt       = submittedAt;
        this.reviewedAt        = reviewedAt;
        this.status            = status;
    }

    public void submit() {
        this.status      = ReportStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    public void approve(int adminId) {
        this.approveByAdmin = adminId;
        this.status         = ReportStatus.APPROVED;
        this.reviewedAt     = LocalDateTime.now();
    }

    public void reject(int adminId) {
        this.approveByAdmin = adminId;
        this.status         = ReportStatus.REJECTED;
        this.reviewedAt     = LocalDateTime.now();
    }

    public int getReportId()              { return reportId; }
    public int getSubmitByTherapist()     { return submitByTherapist; }
    public Integer getApproveByAdmin()    { return approveByAdmin; }
    public String getReportType()         { return reportType; }
    public String getDescription()        { return description; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getReviewedAt()  { return reviewedAt; }
    public ReportStatus getStatus()       { return status; }

    public void setReportId(int id)       { this.reportId = id; }
    public void setDescription(String d)  { this.description = d; }
    public void setStatus(ReportStatus s) { this.status = s; }
    public void setReviewedAt(LocalDateTime t){ this.reviewedAt = t; }
}
