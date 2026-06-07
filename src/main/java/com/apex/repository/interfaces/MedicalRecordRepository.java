package com.apex.repository.interfaces;

import com.apex.domain.ClinicalReport;
import com.apex.domain.Session;
import java.util.List;
import java.util.Optional;

// ISP: Only clinical record and report operations
public interface MedicalRecordRepository {
    void saveReport(ClinicalReport report);
    Optional<ClinicalReport> findReportById(int reportId);
    List<ClinicalReport> findReportsByAthleteId(int athleteId);
    List<ClinicalReport> findReportsByTherapistId(int therapistId);
    void updateReport(ClinicalReport report);
    void deleteReport(int reportId);
    List<Session> getSessionHistory(int athleteId);
}
