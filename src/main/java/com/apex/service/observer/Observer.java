package com.apex.service.observer;

/**
 * Observer Pattern — Subscriber Interface
 * Defines the contract all concrete observers must fulfill.
 * The NotificationEngine depends only on this abstraction,
 * never on concrete observer classes (DIP adherence).
 */
public interface Observer {
    void update(String event);
    int getObserverId();
    String getObserverType();
}
