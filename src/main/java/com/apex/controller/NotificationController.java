package com.apex.controller;

import com.apex.repository.interfaces.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * UC21 notification endpoints for authenticated users.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/{userId}/all")
    public ResponseEntity<?> getAllNotifications(
            @PathVariable int userId,
            @RequestHeader("X-User-Id") int requestUserId) {
        ResponseEntity<?> accessError =
                requireOwnNotifications(userId, requestUserId);
        if (accessError != null) return accessError;
        return ResponseEntity.ok(
                notificationRepository.findByRecipientId(userId));
    }

    @GetMapping("/{userId}/unread")
    public ResponseEntity<?> getUnreadNotifications(
            @PathVariable int userId,
            @RequestHeader("X-User-Id") int requestUserId) {
        ResponseEntity<?> accessError =
                requireOwnNotifications(userId, requestUserId);
        if (accessError != null) return accessError;
        return ResponseEntity.ok(
                notificationRepository.findUnreadByRecipientId(userId));
    }

    @PutMapping("/{notifId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable int notifId,
            @RequestHeader("X-User-Id") int requestUserId) {
        var notification = notificationRepository.findById(notifId);
        if (notification.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ResponseEntity<?> accessError = requireOwnNotifications(
                notification.get().getRecipientId(), requestUserId);
        if (accessError != null) return accessError;
        notificationRepository.markAsRead(notifId);
        return ResponseEntity.ok(Map.of(
                "message", "Notification marked as read"));
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<?> markAllAsRead(
            @PathVariable int userId,
            @RequestHeader("X-User-Id") int requestUserId) {
        ResponseEntity<?> accessError =
                requireOwnNotifications(userId, requestUserId);
        if (accessError != null) return accessError;
        notificationRepository.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of(
                "message", "All notifications marked as read"));
    }

    private ResponseEntity<?> requireOwnNotifications(
            int userId, int requestUserId) {
        if (userId != requestUserId) {
            return ResponseEntity.status(403).body(Map.of(
                    "error",
                    "You can only access your own notifications."));
        }
        return null;
    }
}
