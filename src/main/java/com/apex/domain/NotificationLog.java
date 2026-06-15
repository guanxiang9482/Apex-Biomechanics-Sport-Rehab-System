package com.apex.domain;

import java.time.LocalDateTime;

public class NotificationLog {

    private int notifId;
    private int recipientId;
    private String message;
    private boolean isRead;
    private LocalDateTime timestamp;

    // Constructor for new notification
    public NotificationLog(int recipientId, String message) {
        this.recipientId = recipientId;
        this.message     = message;
        this.isRead      = false;
        this.timestamp   = LocalDateTime.now();
    }

    // Constructor for loading from database
    public NotificationLog(int notifId, int recipientId,
                           String message, boolean isRead,
                           LocalDateTime timestamp) {
        this.notifId     = notifId;
        this.recipientId = recipientId;
        this.message     = message;
        this.isRead      = isRead;
        this.timestamp   = timestamp;
    }

    public void markAsRead() { this.isRead = true; }

    public int getNotifId()             { return notifId; }
    public int getRecipientId()         { return recipientId; }
    public String getMessage()          { return message; }
    public boolean isRead()             { return isRead; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setNotifId(int id)      { this.notifId = id; }
}
