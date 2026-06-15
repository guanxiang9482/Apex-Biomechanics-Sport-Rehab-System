package com.apex.repository.implementation;

import com.apex.domain.Session;
import com.apex.domain.SessionStatus;
import com.apex.repository.interfaces.SessionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlSessionRepository implements SessionRepository {

    private final JdbcTemplate jdbc;

    public MysqlSessionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Session mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        return new Session(
                rs.getInt("session_id"),
                rs.getInt("athlete_id"),
                rs.getInt("therapist_id"),
                rs.getInt("facility_id"),
                rs.getTimestamp("scheduled_date").toLocalDateTime(),
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
                "session_type, scheduled_date, duration_mins, " +
                "status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, session.getAthleteId());
            ps.setInt(2, session.getTherapistId());
            ps.setInt(3, session.getFacilityId());
            ps.setString(4, session.getSessionType());
            ps.setObject(5, session.getSessionDate());
            ps.setInt(6, session.getDurationMins());
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
        var results = jdbc.query(sql, this::mapRow, sessionId);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public List<Session> findByAthleteId(int athleteId) {
        String sql = "SELECT * FROM sessions WHERE athlete_id = ? " +
                "ORDER BY scheduled_date DESC";
        return jdbc.query(sql, this::mapRow, athleteId);
    }

    @Override
    public List<Session> findByTherapistId(int therapistId) {
        String sql = "SELECT * FROM sessions WHERE therapist_id = ? " +
                "ORDER BY scheduled_date ASC";
        return jdbc.query(sql, this::mapRow, therapistId);
    }

    @Override
    public List<Session> findByDate(LocalDate date) {
        String sql = "SELECT * FROM sessions " +
                "WHERE DATE(scheduled_date) = ? " +
                "ORDER BY scheduled_date ASC";
        return jdbc.query(sql, this::mapRow, date);
    }

    @Override
    public List<Session> findUpcomingByAthleteId(int athleteId) {
        String sql = "SELECT * FROM sessions " +
                "WHERE athlete_id = ? " +
                "AND scheduled_date > NOW() " +
                "AND status = 'SCHEDULED' " +
                "ORDER BY scheduled_date ASC";
        return jdbc.query(sql, this::mapRow, athleteId);
    }

    @Override
    public List<Session> findByStatus(SessionStatus status) {
        String sql = "SELECT * FROM sessions WHERE status = ?";
        return jdbc.query(sql, this::mapRow, status.name());
    }

    @Override
    public void updateStatus(int sessionId, SessionStatus status) {
        jdbc.update("UPDATE sessions SET status = ? " +
                "WHERE session_id = ?", status.name(), sessionId);
    }

    @Override
    public void updateSession(Session session) {
        String sql = "UPDATE sessions SET scheduled_date = ?, " +
                "therapist_id = ?, facility_id = ?, " +
                "status = ?, notes = ?, duration_mins = ? " +
                "WHERE session_id = ?";
        jdbc.update(sql,
                session.getSessionDate(),
                session.getTherapistId(),
                session.getFacilityId(),
                session.getStatus().name(),
                session.getNotes(),
                session.getDurationMins(),
                session.getSessionId());
    }

    @Override
    public void delete(int sessionId) {
        jdbc.update("DELETE FROM sessions WHERE session_id = ?",
                sessionId);
    }
}
