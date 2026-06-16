package com.apex.repository.interfaces;

import com.apex.domain.NotificationLog;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    void save(NotificationLog notification);
    Optional<NotificationLog> findById(int notifId);
    List<NotificationLog> findByRecipientId(int recipientId);
    List<NotificationLog> findUnreadByRecipientId(int recipientId);
    void markAsRead(int notifId);
    void markAllAsRead(int recipientId);
}
