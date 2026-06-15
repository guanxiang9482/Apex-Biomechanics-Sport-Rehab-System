package com.apex.service;

import com.apex.domain.Session;
import com.apex.domain.SessionStatus;
import com.apex.repository.interfaces.SessionRepository;
import com.apex.service.observer.NotificationEngine;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SRP: Responsible for rehab session
 * scheduling and lifecycle management.
 * UC5, UC7, UC9, UC11, UC13
 * Triggers Observer on every state change.
 */
@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final NotificationEngine notificationEngine;

    public SessionService(SessionRepository sessionRepository,
                          NotificationEngine notificationEngine) {
        this.sessionRepository  = sessionRepository;
        this.notificationEngine = notificationEngine;
    }

    // UC7 — Book Rehab Session
    public Session bookSession(int athleteId, int therapistId,
                               int facilityId,
                               LocalDateTime scheduledDate,
                               int durationMins,
                               String sessionType) {
        Session session = new Session(athleteId, therapistId,
                facilityId, scheduledDate, durationMins, sessionType);
        sessionRepository.save(session);

        // UC21 — Observer notification
        notificationEngine.notifyAllObservers(
                "New session booked for Athlete #" + athleteId +
                " on " + scheduledDate);
        return session;
    }

    // Used by Facade for initial evaluation (UC15)
    public Session scheduleInitialSession(int athleteId,
                                          int therapistId,
                                          int facilityId) {
        LocalDateTime initial = LocalDateTime.now()
                .plusDays(1).withHour(9).withMinute(0)
                .withSecond(0).withNano(0);
        return bookSession(athleteId, therapistId, facilityId,
                initial, 60, "Initial Evaluation");
    }

    // UC9 — Cancel Session
    public void cancelSession(int sessionId) {
        sessionRepository.findById(sessionId).ifPresent(s -> {
            s.updateStatus(SessionStatus.CANCELLED);
            sessionRepository.updateStatus(sessionId,
                    SessionStatus.CANCELLED);
            notificationEngine.notifyAllObservers(
                    "Session #" + sessionId + " has been CANCELLED.");
        });
    }

    // UC9 — Reschedule Session
    public void rescheduleSession(int sessionId,
                                  LocalDateTime newDate) {
        sessionRepository.findById(sessionId).ifPresent(s -> {
            s.setSessionDate(newDate);
            sessionRepository.updateSession(s);
            notificationEngine.notifyAllObservers(
                    "Session #" + sessionId +
                    " rescheduled to " + newDate);
        });
    }

    // UC13 — Update Session Status
    public void updateStatus(int sessionId,
                             SessionStatus newStatus) {
        sessionRepository.findById(sessionId).ifPresent(s -> {
            s.updateStatus(newStatus);
            sessionRepository.updateStatus(sessionId, newStatus);
            notificationEngine.notifyAllObservers(
                    "Session #" + sessionId +
                    " status updated to " + newStatus.name());
        });
    }

    // UC5 — Today's sessions
    public List<Session> getTodaySessions() {
        return sessionRepository.findByDate(LocalDate.now());
    }

    // UC11 — Therapist daily roster
    public List<Session> getTodayRosterForTherapist(
            int therapistId) {
        return sessionRepository.findByTherapistId(therapistId)
                .stream()
                .filter(s -> s.getSessionDate().toLocalDate()
                        .equals(LocalDate.now()))
                .toList();
    }

    // UC8 — Session history
    public List<Session> getSessionHistory(int athleteId) {
        return sessionRepository.findByAthleteId(athleteId);
    }

    // UC9 — Upcoming sessions
    public List<Session> getUpcomingSessions(int athleteId) {
        return sessionRepository.findUpcomingByAthleteId(athleteId);
    }

    public Optional<Session> findById(int sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public void cancelInitialSession(int athleteId) {
        sessionRepository.findUpcomingByAthleteId(athleteId)
                .stream()
                .filter(s -> "Initial Evaluation"
                        .equals(s.getSessionType()))
                .findFirst()
                .ifPresent(s -> cancelSession(s.getSessionId()));
    }
}
