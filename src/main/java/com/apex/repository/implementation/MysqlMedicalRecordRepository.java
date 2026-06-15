package com.apex.repository.implementation;

import com.apex.domain.MedicalRecord;
import com.apex.repository.interfaces.MedicalRecordRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlMedicalRecordRepository
        implements MedicalRecordRepository {

    private final JdbcTemplate jdbc;

    public MysqlMedicalRecordRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private MedicalRecord mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        return new MedicalRecord(
                rs.getInt("record_id"),
                rs.getInt("athlete_id"),
                rs.getInt("created_by_therapist"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("diagnosis_notes"),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }

    @Override
    public void save(MedicalRecord record) {
        String sql = "INSERT INTO medical_records " +
                "(athlete_id, created_by_therapist, diagnosis_notes) " +
                "VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, record.getAthleteId());
            ps.setInt(2, record.getCreatedByTherapist());
            ps.setString(3, record.getDiagnosisNotes());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            record.setRecordId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public Optional<MedicalRecord> findById(int recordId) {
        String sql = "SELECT * FROM medical_records " +
                "WHERE record_id = ?";
        var results = jdbc.query(sql, this::mapRow, recordId);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public List<MedicalRecord> findByAthleteId(int athleteId) {
        String sql = "SELECT * FROM medical_records " +
                "WHERE athlete_id = ? ORDER BY created_at DESC";
        return jdbc.query(sql, this::mapRow, athleteId);
    }

    @Override
    public List<MedicalRecord> findByTherapistId(int therapistId) {
        String sql = "SELECT * FROM medical_records " +
                "WHERE created_by_therapist = ? " +
                "ORDER BY created_at DESC";
        return jdbc.query(sql, this::mapRow, therapistId);
    }

    @Override
    public void update(MedicalRecord record) {
        String sql = "UPDATE medical_records SET " +
                "diagnosis_notes = ?, updated_at = NOW() " +
                "WHERE record_id = ?";
        jdbc.update(sql, record.getDiagnosisNotes(),
                record.getRecordId());
    }

    @Override
    public void delete(int recordId) {
        jdbc.update("DELETE FROM medical_records " +
                "WHERE record_id = ?", recordId);
    }
}
