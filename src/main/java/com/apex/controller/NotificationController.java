package com.apex.controller;

import com.apex.repository.interfaces.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handler Tier — Notification endpoints
 * RBAC: All authenticated users.
 * UC21: Dispatch and view notifications.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final JdbcTemplate jdbc;

    public NotificationController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // UC21 — Get unread notifications for a user
    @GetMapping("/{userId}/unread")
    public ResponseEntity<?> getUnreadNotifications(
            @PathVariable int userId) {
        String sql = "SELECT * FROM notification_log " +
                     "WHERE recipient_id = ? AND is_read = FALSE " +
                     "ORDER BY created_at DESC";
        List<Map<String, Object>> notifications =
                jdbc.queryForList(sql, userId);
        return ResponseEntity.ok(notifications);
    }

    // UC21 — Mark notification as read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable int notificationId) {
        String sql = "UPDATE notification_log SET is_read = TRUE " +
                     "WHERE notification_id = ?";
        jdbc.update(sql, notificationId);
        return ResponseEntity.ok(Map.of(
                "message", "Notification marked as read"));
    }

    // UC21 — Get all notifications for a user
    @GetMapping("/{userId}/all")
    public ResponseEntity<?> getAllNotifications(
            @PathVariable int userId) {
        String sql = "SELECT * FROM notification_log " +
                     "WHERE recipient_id = ? " +
                     "ORDER BY created_at DESC";
        List<Map<String, Object>> notifications =
                jdbc.queryForList(sql, userId);
        return ResponseEntity.ok(notifications);
    }
}
