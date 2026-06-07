package com.apex.domain;

import java.time.LocalDateTime;

public class NotificationLog {

    private int notificationId;
    private int recipientId;
    private String eventMessage;
    private boolean isRead;
    private LocalDateTime createdAt;

    // Constructor for new notification
    public NotificationLog(int recipientId, String eventMessage) {
        this.recipientId  = recipientId;
        this.eventMessage = eventMessage;
        this.isRead       = false;
        this.createdAt    = LocalDateTime.now();
    }

    // Constructor for loading from database
    public NotificationLog(int notificationId, int recipientId,
                           String eventMessage, boolean isRead,
                           LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.recipientId    = recipientId;
        this.eventMessage   = eventMessage;
        this.isRead         = isRead;
        this.createdAt      = createdAt;
    }

    public void markAsRead() { this.isRead = true; }

    // Getters
    public int getNotificationId()        { return notificationId; }
    public int getRecipientId()           { return recipientId; }
    public String getEventMessage()       { return eventMessage; }
    public boolean isRead()               { return isRead; }
    public LocalDateTime getCreatedAt()   { return createdAt; }

    // Setters
    public void setNotificationId(int id) { this.notificationId = id; }
}
