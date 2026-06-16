package com.apex.repository.implementation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.apex.domain.Administrator;
import com.apex.domain.Athlete;
import com.apex.domain.Physiotherapist;
import com.apex.domain.Role;
import com.apex.domain.User;
import com.apex.repository.interfaces.UserRepository;

@Repository
public class MysqlUserRepository implements UserRepository {

    private final JdbcTemplate jdbc;

    public MysqlUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private User mapRow(ResultSet rs, int rowNum) throws SQLException {
        int userId        = rs.getInt("user_id");
        String username   = rs.getString("username");
        String password   = rs.getString("password");
        String email      = rs.getString("email");
        String fullname   = rs.getString("fullname");
        String contact    = rs.getString("contact");
        boolean isActive  = rs.getBoolean("is_active");
        LocalDateTime lastLogin = rs.getTimestamp("last_login_at") != null
                ? rs.getTimestamp("last_login_at").toLocalDateTime()
                : null;
        LocalDateTime createdAt = rs.getTimestamp("created_at") != null
                ? rs.getTimestamp("created_at").toLocalDateTime()
                : null;
        Role role = Role.valueOf(rs.getString("role"));

        return switch (role) {
            case ATHLETE -> new Athlete(
                    userId, username, password, email,
                    fullname, contact, isActive,
                    lastLogin, createdAt,
                    null, "None", 0, 0, "", 0);
            case THERAPIST -> new Physiotherapist(
                    userId, username, password, email,
                    isActive, lastLogin, createdAt,
                    fullname, "", contact, "", 0);
            case ADMIN -> new Administrator(
                    userId, username, password, email,
                    isActive, lastLogin, createdAt,
                    fullname, contact, "", 0);
        };
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users " +
                "(username, password, email, role, fullname, contact) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbc.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRole().name(),
                user.getFullname(),
                user.getContact());
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        var results = jdbc.query(sql, this::mapRow, username);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public Optional<User> findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        var results = jdbc.query(sql, this::mapRow, userId);
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
    public void updatePassword(int userId, String newPassword) {
        jdbc.update("UPDATE users SET password = ? WHERE user_id = ?",
                newPassword, userId);
    }

    @Override
    public void updateLastLoginAt(int userId) {
        jdbc.update("UPDATE users SET last_login_at = NOW() " +
                "WHERE user_id = ?", userId);
    }

    @Override
    public void updateStaffInfo(int userId, String email, String fullname,
                                String contact) {
        jdbc.update("UPDATE users SET email = ?, fullname = ?, " +
                        "contact = ? WHERE user_id = ?",
                email, fullname, contact, userId);
    }

    @Override
    public void setActiveStatus(int userId, boolean isActive) {
        jdbc.update("UPDATE users SET is_active = ? WHERE user_id = ?",
                isActive, userId);
    }

    @Override
    public void delete(int userId) {
        jdbc.update("DELETE FROM users WHERE user_id = ?", userId);
    }

    @Override
    public List<User> findAllStaff() {
        String sql = "SELECT * FROM users " +
                "WHERE role IN ('THERAPIST', 'ADMIN') " +
                "ORDER BY role, fullname";
        return jdbc.query(sql, this::mapRow);
    }
}
