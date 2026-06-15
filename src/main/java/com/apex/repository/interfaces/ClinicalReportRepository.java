package com.apex.repository.interfaces;

import com.apex.domain.ClinicalReport;
import com.apex.domain.ReportStatus;
import java.util.List;
import java.util.Optional;

public interface ClinicalReportRepository {
    void save(ClinicalReport report);
    Optional<ClinicalReport> findById(int reportId);
    List<ClinicalReport> findByTherapistId(int therapistId);
    List<ClinicalReport> findByStatus(ReportStatus status);
    void update(ClinicalReport report);
    void delete(int reportId);
}
