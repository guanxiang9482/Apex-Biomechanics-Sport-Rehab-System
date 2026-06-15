package com.apex.repository.implementation;

import com.apex.domain.NotificationLog;
import com.apex.repository.interfaces.NotificationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;

@Repository
public class MysqlNotificationRepository
        implements NotificationRepository {

    private final JdbcTemplate jdbc;

    public MysqlNotificationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private NotificationLog mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        return new NotificationLog(
                rs.getInt("notif_id"),
                rs.getInt("recipient_id"),
                rs.getString("message"),
                rs.getBoolean("is_read"),
                rs.getTimestamp("timestamp").toLocalDateTime()
        );
    }

    @Override
    public void save(NotificationLog notification) {
        String sql = "INSERT INTO notifications_log " +
                "(recipient_id, message) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, notification.getRecipientId());
            ps.setString(2, notification.getMessage());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            notification.setNotifId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public List<NotificationLog> findByRecipientId(int recipientId) {
        String sql = "SELECT * FROM notifications_log " +
                "WHERE recipient_id = ? " +
                "ORDER BY timestamp DESC";
        return jdbc.query(sql, this::mapRow, recipientId);
    }

    @Override
    public List<NotificationLog> findUnreadByRecipientId(
            int recipientId) {
        String sql = "SELECT * FROM notifications_log " +
                "WHERE recipient_id = ? AND is_read = FALSE " +
                "ORDER BY timestamp DESC";
        return jdbc.query(sql, this::mapRow, recipientId);
    }

    @Override
    public void markAsRead(int notifId) {
        jdbc.update("UPDATE notifications_log SET is_read = TRUE " +
                "WHERE notif_id = ?", notifId);
    }

    @Override
    public void markAllAsRead(int recipientId) {
        jdbc.update("UPDATE notifications_log SET is_read = TRUE " +
                "WHERE recipient_id = ?", recipientId);
    }
}
