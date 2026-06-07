package com.apex.service.observer;

import com.apex.domain.NotificationLog;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Observer — Physiotherapist Subscriber
 * Receives schedule change events affecting
 * the therapist's daily roster.
 */
public class TherapistObserver implements Observer {

    private final int therapistId;
    private final List<NotificationLog> notifyLog;

    public TherapistObserver(int therapistId) {
        this.therapistId = therapistId;
        this.notifyLog   = new ArrayList<>();
    }

    @Override
    public void update(String event) {
        NotificationLog log = new NotificationLog(therapistId, event);
        notifyLog.add(log);
        System.out.println("[THERAPIST NOTIFICATION | ID:" +
                therapistId + "] " + event);
    }

    @Override
    public int getObserverId() { return therapistId; }

    @Override
    public String getObserverType() { return "THERAPIST"; }

    public List<NotificationLog> getNotifyLog() { return notifyLog; }
}
