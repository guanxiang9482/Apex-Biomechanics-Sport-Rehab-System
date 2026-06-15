package com.apex.controller;

import com.apex.repository.interfaces.NotificationRepository;
import com.apex.domain.NotificationLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UC21 — Notification endpoints
 * All authenticated users.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // UC21 — Get all notifications
    @GetMapping("/{userId}/all")
    public ResponseEntity<List<NotificationLog>>
    getAllNotifications(@PathVariable int userId) {
        return ResponseEntity.ok(
                notificationRepository
                        .findByRecipientId(userId));
    }

    // UC21 — Get unread notifications
    @GetMapping("/{userId}/unread")
    public ResponseEntity<List<NotificationLog>>
    getUnreadNotifications(@PathVariable int userId) {
        return ResponseEntity.ok(
                notificationRepository
                        .findUnreadByRecipientId(userId));
    }

    // UC21 — Mark as read
    @PutMapping("/{notifId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable int notifId) {
        notificationRepository.markAsRead(notifId);
        return ResponseEntity.ok(Map.of(
                "message", "Notification marked as read"));
    }

    // UC21 — Mark all as read
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<?> markAllAsRead(
            @PathVariable int userId) {
        notificationRepository.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of(
                "message", "All notifications marked as read"));
    }
}
