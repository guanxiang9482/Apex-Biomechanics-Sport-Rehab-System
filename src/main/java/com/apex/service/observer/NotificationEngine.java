package com.apex.service.observer;

import com.apex.domain.NotificationLog;
import com.apex.repository.interfaces.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer Pattern — Publisher (Subject)
 * Maintains a dynamic list of subscribers and broadcasts
 * events to all registered observers.
 *
 * Key design decisions:
 * - Depends only on Observer interface, never concrete classes (DIP)
 * - Subscribers added/removed at runtime (OCP adherence)
 * - Persists all notifications to database for audit trail
 */
@Service
public class NotificationEngine {

    private final List<Observer> observers;
    private final List<String> eventLog;
    private final JdbcTemplate jdbc;

    public NotificationEngine(JdbcTemplate jdbc) {
        this.observers = new ArrayList<>();
        this.eventLog  = new ArrayList<>();
        this.jdbc      = jdbc;
    }

    // Subscribe an observer at runtime
    public void subscribe(Observer observer) {
        observers.add(observer);
    }

    // Unsubscribe — prevents memory leak (lapsed listener problem)
    public void unsubscribe(Observer observer) {
        observers.remove(observer);
    }

    // Unsubscribe by ID when user logs out
    public void unsubscribeById(int observerId) {
        observers.removeIf(o -> o.getObserverId() == observerId);
    }

    /**
     * Core broadcast method — UC21: Dispatch Notifications
     * Iterates all subscribers and delegates update to each.
     * NotificationEngine remains unaware of observer internals.
     */
    public void notifyAllObservers(String event) {
        eventLog.add(event);
        for (Observer observer : observers) {
            observer.update(event);
        }
        // Persist to notification_log table for all observers
        persistNotifications(event);
    }

    // Persist notification to database for each registered observer
    private void persistNotifications(String event) {
        String sql = "INSERT INTO notification_log " +
                     "(recipient_id, event_message) VALUES (?, ?)";
        for (Observer observer : observers) {
            jdbc.update(sql, observer.getObserverId(), event);
        }
    }

    // Notify a specific single observer by ID
    public void notifyObserver(int observerId, String event) {
        observers.stream()
                .filter(o -> o.getObserverId() == observerId)
                .findFirst()
                .ifPresent(o -> {
                    o.update(event);
                    String sql = "INSERT INTO notification_log " +
                                 "(recipient_id, event_message) " +
                                 "VALUES (?, ?)";
                    jdbc.update(sql, observerId, event);
                });
    }

    public List<String> getEventLog()  { return eventLog; }
    public boolean isEmpty()           { return observers.isEmpty(); }
    public int getObserverCount()      { return observers.size(); }

    public void clearEventLog() { eventLog.clear(); }
}
