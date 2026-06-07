package com.apex.repository.implementation;

import com.apex.domain.Athlete;
import com.apex.repository.interfaces.AthleteRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlAthleteRepository implements AthleteRepository {

    private final JdbcTemplate jdbc;

    public MysqlAthleteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Athlete mapRowToAthlete(ResultSet rs, int rowNum)
            throws SQLException {
        return new Athlete(
                rs.getInt("athlete_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("email"),
                rs.getBoolean("is_active"),
                rs.getTimestamp("last_active") != null
                        ? rs.getTimestamp("last_active").toLocalDateTime()
                        : null,
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("full_name"),
                rs.getDate("date_of_birth") != null
                        ? rs.getDate("date_of_birth").toLocalDate()
                        : null,
                rs.getString("phone"),
                rs.getString("injury_status"),
                rs.getDouble("body_weight_kg"),
                rs.getDouble("height_cm"),
                rs.getString("posture_notes")
        );
    }

    @Override
    public void save(Athlete athlete) {
        String sql = "INSERT INTO athletes " +
                     "(athlete_id, full_name, date_of_birth, phone, " +
                     "injury_status, body_weight_kg, height_cm, " +
                     "posture_notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql,
                athlete.getUserId(),
                athlete.getFullName(),
                athlete.getDateOfBirth(),
                athlete.getPhone(),
                athlete.getInjuryStatus(),
                athlete.getBodyWeightKg(),
                athlete.getHeightCm(),
                athlete.getPostureNotes());
    }

    @Override
    public Optional<Athlete> findById(int athleteId) {
        String sql = "SELECT u.*, a.* FROM users u " +
                     "JOIN athletes a ON u.user_id = a.athlete_id " +
                     "WHERE a.athlete_id = ?";
        var results = jdbc.query(sql, this::mapRowToAthlete, athleteId);
        return results.isEmpty() ? Optional.empty()
                                 : Optional.of(results.get(0));
    }

    @Override
    public List<Athlete> findAll() {
        String sql = "SELECT u.*, a.* FROM users u " +
                     "JOIN athletes a ON u.user_id = a.athlete_id " +
                     "WHERE u.is_active = TRUE";
        return jdbc.query(sql, this::mapRowToAthlete);
    }

    @Override
    public void updateProfile(Athlete athlete) {
        String sql = "UPDATE athletes SET full_name = ?, " +
                     "date_of_birth = ?, phone = ?, injury_status = ?, " +
                     "body_weight_kg = ?, height_cm = ?, " +
                     "posture_notes = ? WHERE athlete_id = ?";
        jdbc.update(sql,
                athlete.getFullName(),
                athlete.getDateOfBirth(),
                athlete.getPhone(),
                athlete.getInjuryStatus(),
                athlete.getBodyWeightKg(),
                athlete.getHeightCm(),
                athlete.getPostureNotes(),
                athlete.getUserId());
    }

    @Override
    public void delete(int athleteId) {
        String sql = "DELETE FROM athletes WHERE athlete_id = ?";
        jdbc.update(sql, athleteId);
    }
}
