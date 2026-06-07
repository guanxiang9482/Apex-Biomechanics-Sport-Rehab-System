package com.apex.service.observer;

import com.apex.domain.NotificationLog;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Observer — Administrator Subscriber
 * Receives all clinical events for audit logging
 * and operational monitoring.
 */
public class AdminObserver implements Observer {

    private final int adminId;
    private final List<NotificationLog> auditLog;

    public AdminObserver(int adminId) {
        this.adminId  = adminId;
        this.auditLog = new ArrayList<>();
    }

    @Override
    public void update(String event) {
        NotificationLog log = new NotificationLog(adminId, event);
        auditLog.add(log);
        System.out.println("[ADMIN AUDIT LOG | ID:" +
                adminId + "] " + event);
    }

    @Override
    public int getObserverId() { return adminId; }

    @Override
    public String getObserverType() { return "ADMIN"; }

    public List<NotificationLog> getAuditLog() { return auditLog; }
}
