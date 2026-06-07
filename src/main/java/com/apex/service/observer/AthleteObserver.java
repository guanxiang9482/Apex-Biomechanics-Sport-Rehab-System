package com.apex.service.observer;

import com.apex.domain.NotificationLog;
import com.apex.repository.interfaces.UserRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Observer — Athlete Subscriber
 * Receives and logs events relevant to the athlete's
 * schedule and billing updates.
 */
public class AthleteObserver implements Observer {

    private final int athleteId;
    private final List<NotificationLog> notifyLog;
    private final UserRepository userRepository;

    public AthleteObserver(int athleteId, UserRepository userRepository) {
        this.athleteId      = athleteId;
        this.userRepository = userRepository;
        this.notifyLog      = new ArrayList<>();
    }

    @Override
    public void update(String event) {
        NotificationLog log = new NotificationLog(athleteId, event);
        notifyLog.add(log);
        // Persist notification to database
        // userRepository handles recipient lookup
        System.out.println("[ATHLETE NOTIFICATION | ID:" +
                athleteId + "] " + event);
    }

    @Override
    public int getObserverId() { return athleteId; }

    @Override
    public String getObserverType() { return "ATHLETE"; }

    public List<NotificationLog> getNotifyLog() { return notifyLog; }
}
