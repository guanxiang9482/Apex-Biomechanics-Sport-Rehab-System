package com.apex.repository.implementation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.apex.domain.Athlete;
import com.apex.repository.interfaces.AthleteRepository;

@Repository
public class MysqlAthleteRepository implements AthleteRepository {

    private final JdbcTemplate jdbc;

    public MysqlAthleteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Athlete mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        return new Athlete(
                rs.getInt("user_id"),           // userId
                rs.getString("username"),        // username
                rs.getString("password"),        // password
                rs.getString("email"),           // email
                rs.getString("fullname"),        // fullname
                rs.getString("contact"),         // contact
                rs.getBoolean("is_active"),      // isActive
                rs.getTimestamp("last_login_at") != null
                        ? rs.getTimestamp("last_login_at")
                        .toLocalDateTime() : null, // lastActive
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at")
                        .toLocalDateTime() : null, // createdAt
                rs.getDate("date_of_birth") != null
                        ? rs.getDate("date_of_birth")
                        .toLocalDate() : null,   // dateOfBirth
                rs.getString("injury_status"),   // injuryStatus
                rs.getDouble("body_weight_kg"),  // bodyWeightKg
                rs.getDouble("height_cm"),       // heightCm
                rs.getString("sport"),           // sport
                rs.getInt("athlete_id")           // athleteId (end)
        );
    }

    @Override
    public void save(Athlete athlete) {
        String sql = "INSERT INTO athletes " +
                "(user_id, date_of_birth, sport, injury_status, " +
                "body_weight_kg, height_cm) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, athlete.getUserId());
            ps.setObject(2, athlete.getDateOfBirth());
            ps.setString(3, athlete.getSport());
            ps.setString(4, athlete.getInjuryStatus());
            ps.setDouble(5, athlete.getBodyWeightKg());
            ps.setDouble(6, athlete.getHeightCm());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            athlete.setAthleteId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public Optional<Athlete> findById(int athleteId) {
        String sql = "SELECT u.*, a.* FROM users u " +
                "JOIN athletes a ON u.user_id = a.user_id " +
                "WHERE a.athlete_id = ?";
        var results = jdbc.query(sql, this::mapRow, athleteId);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public Optional<Athlete> findByUserId(int userId) {
        String sql = "SELECT u.*, a.* FROM users u " +
                "JOIN athletes a ON u.user_id = a.user_id " +
                "WHERE u.user_id = ?";
        var results = jdbc.query(sql, this::mapRow, userId);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public List<Athlete> findAll() {
        String sql = "SELECT u.*, a.* FROM users u " +
                "JOIN athletes a ON u.user_id = a.user_id " +
                "WHERE u.is_active = TRUE " +
                "ORDER BY a.athlete_id";
        return jdbc.query(sql, this::mapRow);
    }

    @Override
    public void updateProfile(Athlete athlete) {
        String sql = "UPDATE athletes SET date_of_birth = ?, " +
                "sport = ?, injury_status = ?, " +
                "body_weight_kg = ?, height_cm = ? " +
                "WHERE athlete_id = ?";
        jdbc.update(sql,
                athlete.getDateOfBirth(),
                athlete.getSport(),
                athlete.getInjuryStatus(),
                athlete.getBodyWeightKg(),
                athlete.getHeightCm(),
                athlete.getAthleteId());
    }

    @Override
    public void delete(int athleteId) {
        jdbc.update("DELETE FROM athletes WHERE athlete_id = ?",
                athleteId);
    }
}
