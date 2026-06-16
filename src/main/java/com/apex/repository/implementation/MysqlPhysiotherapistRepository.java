package com.apex.repository.implementation;

import com.apex.domain.Physiotherapist;
import com.apex.repository.interfaces.PhysiotherapistRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlPhysiotherapistRepository implements PhysiotherapistRepository {

    private final JdbcTemplate jdbc;

    public MysqlPhysiotherapistRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Physiotherapist mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        LocalDateTime lastLogin = rs.getTimestamp("last_login_at") != null
                ? rs.getTimestamp("last_login_at").toLocalDateTime()
                : null;
        LocalDateTime createdAt = rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime()
                : null;

        return new Physiotherapist(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getBoolean("is_active"),
                lastLogin,
                createdAt,
                rs.getString("fullname"),
                rs.getString("specialization"),
                rs.getString("contact"),
                rs.getString("license_number"),
                rs.getInt("therapist_id")
        );
    }

    @Override
    public Optional<Physiotherapist> findById(int therapistId) {
        String sql = """
                SELECT u.*, p.therapist_id, p.specialization, p.license_number
                FROM physiotherapists p
                JOIN users u ON u.user_id = p.user_id
                WHERE p.therapist_id = ?
                """;
        var results = jdbc.query(sql, this::mapRow, therapistId);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public Optional<Physiotherapist> findByUserId(int userId) {
        String sql = """
                SELECT u.*, p.therapist_id, p.specialization, p.license_number
                FROM physiotherapists p
                JOIN users u ON u.user_id = p.user_id
                WHERE p.user_id = ?
                """;
        var results = jdbc.query(sql, this::mapRow, userId);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public List<Physiotherapist> findAll() {
        String sql = """
                SELECT u.*, p.therapist_id, p.specialization, p.license_number
                FROM physiotherapists p
                JOIN users u ON u.user_id = p.user_id
                WHERE u.is_active = TRUE
                ORDER BY u.fullname
                """;
        return jdbc.query(sql, this::mapRow);
    }

    @Override
    public void updateProfessionalInfo(int userId, String specialization,
                                       String licenseNumber) {
        jdbc.update("UPDATE physiotherapists SET specialization = ?, " +
                        "license_number = ? WHERE user_id = ?",
                specialization, licenseNumber, userId);
    }
}
