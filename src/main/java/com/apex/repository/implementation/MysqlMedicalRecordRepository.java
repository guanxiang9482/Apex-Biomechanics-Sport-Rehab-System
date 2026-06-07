package com.apex.repository.implementation;

import com.apex.domain.ClinicalReport;
import com.apex.domain.ReportStatus;
import com.apex.domain.Session;
import com.apex.domain.SessionStatus;
import com.apex.repository.interfaces.MedicalRecordRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlMedicalRecordRepository implements MedicalRecordRepository {

    private final JdbcTemplate jdbc;

    public MysqlMedicalRecordRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private ClinicalReport mapRowToReport(ResultSet rs, int rowNum)
            throws SQLException {
        return new ClinicalReport(
                rs.getInt("report_id"),
                rs.getInt("athlete_id"),
                rs.getInt("therapist_id"),
                rs.getTimestamp("report_date").toLocalDateTime(),
                rs.getString("summary"),
                ReportStatus.valueOf(rs.getString("status"))
        );
    }

    private Session mapRowToSession(ResultSet rs, int rowNum)
            throws SQLException {
        return new Session(
                rs.getInt("session_id"),
                rs.getInt("athlete_id"),
                rs.getInt("therapist_id"),
                rs.getInt("facility_id"),
                rs.getTimestamp("session_date").toLocalDateTime(),
                rs.getInt("duration_mins"),
                rs.getString("session_type"),
                SessionStatus.valueOf(rs.getString("status")),
                rs.getString("notes"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    @Override
    public void saveReport(ClinicalReport report) {
        String sql = "INSERT INTO clinical_reports " +
                     "(athlete_id, therapist_id, summary, status) " +
                     "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, report.getAthleteId());
            ps.setInt(2, report.getTherapistId());
            ps.setString(3, report.getSummary());
            ps.setString(4, report.getStatus().name());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            report.setReportId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public Optional<ClinicalReport> findReportById(int reportId) {
        String sql = "SELECT * FROM clinical_reports WHERE report_id = ?";
        var results = jdbc.query(sql, this::mapRowToReport, reportId);
        return results.isEmpty() ? Optional.empty()
                                 : Optional.of(results.get(0));
    }

    @Override
    public List<ClinicalReport> findReportsByAthleteId(int athleteId) {
        String sql = "SELECT * FROM clinical_reports " +
                     "WHERE athlete_id = ? ORDER BY report_date DESC";
        return jdbc.query(sql, this::mapRowToReport, athleteId);
    }

    @Override
    public List<ClinicalReport> findReportsByTherapistId(int therapistId) {
        String sql = "SELECT * FROM clinical_reports " +
                     "WHERE therapist_id = ? ORDER BY report_date DESC";
        return jdbc.query(sql, this::mapRowToReport, therapistId);
    }

    @Override
    public void updateReport(ClinicalReport report) {
        String sql = "UPDATE clinical_reports SET summary = ?, " +
                     "status = ? WHERE report_id = ?";
        jdbc.update(sql,
                report.getSummary(),
                report.getStatus().name(),
                report.getReportId());
    }

    @Override
    public void deleteReport(int reportId) {
        String sql = "DELETE FROM clinical_reports WHERE report_id = ?";
        jdbc.update(sql, reportId);
    }

    @Override
    public List<Session> getSessionHistory(int athleteId) {
        String sql = "SELECT * FROM sessions WHERE athlete_id = ? " +
                     "AND status = 'COMPLETED' " +
                     "ORDER BY session_date DESC";
        return jdbc.query(sql, this::mapRowToSession, athleteId);
    }
}
