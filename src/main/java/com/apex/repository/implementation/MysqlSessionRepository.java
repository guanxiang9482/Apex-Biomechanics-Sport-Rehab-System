package com.apex.repository.implementation;

import com.apex.domain.Session;
import com.apex.domain.SessionStatus;
import com.apex.repository.interfaces.SessionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlSessionRepository implements SessionRepository {

    private final JdbcTemplate jdbc;

    public MysqlSessionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
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
    public void save(Session session) {
        String sql = "INSERT INTO sessions " +
                     "(athlete_id, therapist_id, facility_id, " +
                     "session_date, duration_mins, session_type, " +
                     "status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, session.getAthleteId());
            ps.setInt(2, session.getTherapistId());
            ps.setInt(3, session.getFacilityId());
            ps.setObject(4, session.getSessionDate());
            ps.setInt(5, session.getDurationMins());
            ps.setString(6, session.getSessionType());
            ps.setString(7, session.getStatus().name());
            ps.setString(8, session.getNotes());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            session.setSessionId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public Optional<Session> findById(int sessionId) {
        String sql = "SELECT * FROM sessions WHERE session_id = ?";
        var results = jdbc.query(sql, this::mapRowToSession, sessionId);
        return results.isEmpty() ? Optional.empty()
                                 : Optional.of(results.get(0));
    }

    @Override
    public List<Session> findByAthleteId(int athleteId) {
        String sql = "SELECT * FROM sessions WHERE athlete_id = ? " +
                     "ORDER BY session_date DESC";
        return jdbc.query(sql, this::mapRowToSession, athleteId);
    }

    @Override
    public List<Session> findByTherapistId(int therapistId) {
        String sql = "SELECT * FROM sessions WHERE therapist_id = ? " +
                     "ORDER BY session_date ASC";
        return jdbc.query(sql, this::mapRowToSession, therapistId);
    }

    @Override
    public List<Session> findByDate(LocalDate date) {
        String sql = "SELECT * FROM sessions " +
                     "WHERE DATE(session_date) = ? " +
                     "ORDER BY session_date ASC";
        return jdbc.query(sql, this::mapRowToSession, date);
    }

    @Override
    public List<Session> findUpcomingByAthleteId(int athleteId) {
        String sql = "SELECT * FROM sessions " +
                     "WHERE athlete_id = ? " +
                     "AND session_date > NOW() " +
                     "AND status = 'SCHEDULED' " +
                     "ORDER BY session_date ASC";
        return jdbc.query(sql, this::mapRowToSession, athleteId);
    }

    @Override
    public List<Session> findByStatus(SessionStatus status) {
        String sql = "SELECT * FROM sessions WHERE status = ?";
        return jdbc.query(sql, this::mapRowToSession, status.name());
    }

    @Override
    public void updateStatus(int sessionId, SessionStatus status) {
        String sql = "UPDATE sessions SET status = ? " +
                     "WHERE session_id = ?";
        jdbc.update(sql, status.name(), sessionId);
    }

    @Override
    public void updateSession(Session session) {
        String sql = "UPDATE sessions SET session_date = ?, " +
                     "therapist_id = ?, facility_id = ?, " +
                     "status = ?, notes = ? WHERE session_id = ?";
        jdbc.update(sql,
                session.getSessionDate(),
                session.getTherapistId(),
                session.getFacilityId(),
                session.getStatus().name(),
                session.getNotes(),
                session.getSessionId());
    }

    @Override
    public void delete(int sessionId) {
        String sql = "DELETE FROM sessions WHERE session_id = ?";
        jdbc.update(sql, sessionId);
    }
}
