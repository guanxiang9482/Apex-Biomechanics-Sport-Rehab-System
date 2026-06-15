package com.apex.repository.implementation;

import com.apex.domain.BiomechanicalRecord;
import com.apex.repository.interfaces.BiomechanicsRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlBiomechanicsRepository implements BiomechanicsRepository {

    private final JdbcTemplate jdbc;

    public MysqlBiomechanicsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private BiomechanicalRecord mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        return new BiomechanicalRecord(
                rs.getInt("record_id"),
                rs.getInt("athlete_id"),
                rs.getInt("therapist_id"),
                rs.getInt("session_id"),
                rs.getDouble("jump_power"),
                rs.getDouble("joint_mobility"),
                rs.getDouble("posture_score"),
                rs.getTimestamp("recorded_at").toLocalDateTime(),
                rs.getString("treatment_note")
        );
    }

    @Override
    public void save(BiomechanicalRecord record) {
        String sql = "INSERT INTO biomechanical_records " +
                "(athlete_id, therapist_id, session_id, " +
                "jump_power, joint_mobility, posture_score, " +
                "treatment_note) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, record.getAthleteId());
            ps.setInt(2, record.getTherapistId());
            ps.setInt(3, record.getSessionId());
            ps.setDouble(4, record.getJumpPower());
            ps.setDouble(5, record.getJointMobility());
            ps.setDouble(6, record.getPostureScore());
            ps.setString(7, record.getTreatmentNote());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            record.setRecordId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public Optional<BiomechanicalRecord> findById(int recordId) {
        String sql = "SELECT * FROM biomechanical_records " +
                "WHERE record_id = ?";
        var results = jdbc.query(sql, this::mapRow, recordId);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public List<BiomechanicalRecord> findBySessionId(int sessionId) {
        String sql = "SELECT * FROM biomechanical_records " +
                "WHERE session_id = ? ORDER BY recorded_at DESC";
        return jdbc.query(sql, this::mapRow, sessionId);
    }

    @Override
    public List<BiomechanicalRecord> findByAthleteId(int athleteId) {
        String sql = "SELECT * FROM biomechanical_records " +
                "WHERE athlete_id = ? ORDER BY recorded_at DESC";
        return jdbc.query(sql, this::mapRow, athleteId);
    }

    @Override
    public List<BiomechanicalRecord> findByTherapistId(int therapistId) {
        String sql = "SELECT * FROM biomechanical_records " +
                "WHERE therapist_id = ? ORDER BY recorded_at DESC";
        return jdbc.query(sql, this::mapRow, therapistId);
    }

    @Override
    public void update(BiomechanicalRecord record) {
        String sql = "UPDATE biomechanical_records SET " +
                "jump_power = ?, joint_mobility = ?, " +
                "posture_score = ?, treatment_note = ? " +
                "WHERE record_id = ?";
        jdbc.update(sql,
                record.getJumpPower(),
                record.getJointMobility(),
                record.getPostureScore(),
                record.getTreatmentNote(),
                record.getRecordId());
    }

    @Override
    public void delete(int recordId) {
        jdbc.update("DELETE FROM biomechanical_records " +
                "WHERE record_id = ?", recordId);
    }
}
