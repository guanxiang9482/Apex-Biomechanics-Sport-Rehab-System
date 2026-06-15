package com.apex.service.observer;

import com.apex.domain.NotificationLog;
import com.apex.repository.interfaces.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer Pattern — Publisher (Subject)
 * Maintains subscriber list and broadcasts events.
 *
 * Design decisions:
 * - Depends only on Observer interface, not concrete
 *   classes (DIP adherence)
 * - Subscribers added/removed at runtime (OCP adherence)
 * - Persists all notifications via NotificationRepository
 */
@Service
public class NotificationEngine {

    private final List<Observer> observers;
    private final List<String> eventLog;
    private final NotificationRepository notificationRepository;

    public NotificationEngine(
            NotificationRepository notificationRepository) {
        this.observers              = new ArrayList<>();
        this.eventLog               = new ArrayList<>();
        this.notificationRepository = notificationRepository;
    }

    public void subscribe(Observer observer) {
        observers.add(observer);
    }

    public void unsubscribe(Observer observer) {
        observers.remove(observer);
    }

    public void unsubscribeById(int observerId) {
        observers.removeIf(
                o -> o.getObserverId() == observerId);
    }

    // UC21 — Broadcast to all subscribers
    public void notifyAllObservers(String event) {
        eventLog.add(event);
        for (Observer observer : observers) {
            observer.update(event);
            // Persist each notification
            notificationRepository.save(
                    new NotificationLog(
                            observer.getObserverId(), event));
        }
    }

    // Notify a specific subscriber by ID
    public void notifyObserver(int observerId, String event) {
        observers.stream()
                .filter(o -> o.getObserverId() == observerId)
                .findFirst()
                .ifPresent(o -> {
                    o.update(event);
                    notificationRepository.save(
                            new NotificationLog(observerId, event));
                });
    }

    public List<String> getEventLog()  { return eventLog; }
    public boolean isEmpty()           { return observers.isEmpty(); }
    public int getObserverCount()      { return observers.size(); }
    public void clearEventLog()        { eventLog.clear(); }
}
