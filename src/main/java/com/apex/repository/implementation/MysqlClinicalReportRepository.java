package com.apex.repository.implementation;

import com.apex.domain.ClinicalReport;
import com.apex.domain.ReportStatus;
import com.apex.repository.interfaces.ClinicalReportRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlClinicalReportRepository
        implements ClinicalReportRepository {

    private final JdbcTemplate jdbc;

    public MysqlClinicalReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private ClinicalReport mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        return new ClinicalReport(
                rs.getInt("report_id"),
                rs.getInt("submit_by_therapist"),
                rs.getObject("approve_by_admin") != null
                        ? rs.getInt("approve_by_admin") : null,
                rs.getString("report_type"),
                rs.getString("description"),
                rs.getTimestamp("submitted_at").toLocalDateTime(),
                rs.getTimestamp("reviewed_at") != null
                        ? rs.getTimestamp("reviewed_at")
                        .toLocalDateTime() : null,
                ReportStatus.valueOf(rs.getString("status"))
        );
    }

    @Override
    public void save(ClinicalReport report) {
        String sql = "INSERT INTO clinical_reports " +
                "(submit_by_therapist, report_type, " +
                "description, status) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, report.getSubmitByTherapist());
            ps.setString(2, report.getReportType());
            ps.setString(3, report.getDescription());
            ps.setString(4, report.getStatus().name());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            report.setReportId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public Optional<ClinicalReport> findById(int reportId) {
        String sql = "SELECT * FROM clinical_reports " +
                "WHERE report_id = ?";
        var results = jdbc.query(sql, this::mapRow, reportId);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public List<ClinicalReport> findByTherapistId(int therapistId) {
        String sql = "SELECT * FROM clinical_reports " +
                "WHERE submit_by_therapist = ? " +
                "ORDER BY submitted_at DESC";
        return jdbc.query(sql, this::mapRow, therapistId);
    }

    @Override
    public List<ClinicalReport> findByStatus(ReportStatus status) {
        String sql = "SELECT * FROM clinical_reports " +
                "WHERE status = ? ORDER BY submitted_at DESC";
        return jdbc.query(sql, this::mapRow, status.name());
    }

    @Override
    public void update(ClinicalReport report) {
        String sql = "UPDATE clinical_reports SET " +
                "approve_by_admin = ?, description = ?, " +
                "status = ?, reviewed_at = ? " +
                "WHERE report_id = ?";
        jdbc.update(sql,
                report.getApproveByAdmin(),
                report.getDescription(),
                report.getStatus().name(),
                report.getReviewedAt(),
                report.getReportId());
    }

    @Override
    public void delete(int reportId) {
        jdbc.update("DELETE FROM clinical_reports " +
                "WHERE report_id = ?", reportId);
    }
}
