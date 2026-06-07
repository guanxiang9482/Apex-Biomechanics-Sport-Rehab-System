package com.apex.repository.implementation;

import com.apex.domain.*;
import com.apex.repository.interfaces.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class MysqlUserRepository implements UserRepository {

    private final JdbcTemplate jdbc;

    public MysqlUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // RowMapper — maps ResultSet rows to User objects
    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));
        int userId = rs.getInt("user_id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String email = rs.getString("email");
        boolean isActive = rs.getBoolean("is_active");
        var lastActive = rs.getTimestamp("last_active") != null
                ? rs.getTimestamp("last_active").toLocalDateTime() : null;
        var createdAt = rs.getTimestamp("created_at").toLocalDateTime();

        return switch (role) {
            case ATHLETE -> new Athlete(userId, username, passwordHash,
                    email, isActive, lastActive, createdAt,
                    "", null, "", "None", 0, 0, "");
            case THERAPIST -> new Physiotherapist(userId, username, passwordHash,
                    email, isActive, lastActive, createdAt,
                    "", "", "", "");
            case ADMIN -> new Administrator(userId, username, passwordHash,
                    email, isActive, lastActive, createdAt,
                    "", "", "");
        };
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users " +
                     "(username, password_hash, email, role) " +
                     "VALUES (?, ?, ?, ?)";
        jdbc.update(sql,
                user.getUsername(),
                user.getPasswordHash(),
                user.getEmail(),
                user.getRole().name());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        var results = jdbc.query(sql, this::mapRowToUser, username);
        return results.isEmpty() ? Optional.empty()
                                 : Optional.of(results.get(0));
    }

    @Override
    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        var results = jdbc.query(sql, this::mapRowToUser, userId);
        return results.isEmpty() ? Optional.empty()
                                 : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public void updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? " +
                     "WHERE user_id = ?";
        jdbc.update(sql, newPasswordHash, userId);
    }

    @Override
    public void updateLastActive(int userId) {
        String sql = "UPDATE users SET last_active = NOW() " +
                     "WHERE user_id = ?";
        jdbc.update(sql, userId);
    }

    @Override
    public void setActiveStatus(int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
        jdbc.update(sql, isActive, userId);
    }

    @Override
    public void delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbc.update(sql, userId);
    }
}
