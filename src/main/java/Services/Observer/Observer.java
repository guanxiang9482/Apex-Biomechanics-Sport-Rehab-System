package main.java.Services.Observer;

import java.util.List;

// Subscriber interface for Observer pattern
public interface Observer {
    void update(String event);
    int getObserverId();
}

class NotificationEngine{
    private List<Observer> observers;
    private List<String> eventLog;
    private String mainState;

    public NotificationEngine() {
        this.observers = new java.util.ArrayList<>();
        this.eventLog = new java.util.ArrayList<>();
        this.mainState = "Initial State";
    }

    public void subscribe(Observer observer){
        observers.add(observer);
    }
    public void unsubscribe(Observer observer){
        observers.remove(observer);
    }
    public List<String> getEventLog(){
        return eventLog;
    }
    public void clearEventLog(){
        eventLog.clear();
    }
    public void notifyAll(String event){
        for(Observer observer : observers){
            observer.update(event);
        }
    }
    public boolean isEmpty(){
        return observers.isEmpty();
    }
}

// Concrete subscriber
class AthleteObserver implements Observer {
    private int athleteId;
    private List<String> notifyLog;

    public AthleteObserver(int athleteId) {
        this.athleteId = athleteId;
    }

    @Override
    public void update(String event) {
        // Here we would notify the athlete about the event
        notifyLog.add("Event " + notifyLog.size() + ": " + event);
    }

    @Override
    public int getObserverId() {
        return athleteId;
    }
}

// Concrete subscriber
class AdminObserver implements Observer {
    private int adminId;
    private List<String> auditLog;
    public AdminObserver(int adminId) {
        this.adminId = adminId;
    }

    @Override
    public void update(String event) {
        // Here we would notify the admin about the event
        auditLog.add("Event " + auditLog.size() + ": " + event);
    }

    @Override
    public int getObserverId() {
        return adminId;
    }
}

// Concrete subscriber
class TherapistObserver implements Observer {
    private int therapistId;
    private List<String> notifyLog;

    public TherapistObserver(int therapistId) {
        this.therapistId = therapistId;
    }

    @Override
    public void update(String event) {
        // Here we would notify the therapist about the event
        notifyLog.add("Event " + notifyLog.size() + ": " + event);
    }

    @Override
    public int getObserverId() {
        return therapistId;
    }
}