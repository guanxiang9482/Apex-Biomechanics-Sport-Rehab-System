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
 * SRP: Responsible for rehab session scheduling logic.
 * Handles UC5, UC7, UC9, UC11, UC13.
 * Triggers Observer notifications on state changes.
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
                               int facilityId, LocalDateTime sessionDate,
                               int durationMins, String sessionType) {
        Session session = new Session(athleteId, therapistId,
                facilityId, sessionDate, durationMins, sessionType);
        sessionRepository.save(session);

        // UC21 — Trigger Observer notification
        notificationEngine.notifyAllObservers(
                "New session booked for Athlete ID:" + athleteId +
                " on " + sessionDate);
        return session;
    }

    // Used by Facade for initial evaluation session (UC15)
    public Session scheduleInitialSession(int athleteId,
                                          int therapistId,
                                          int facilityId) {
        LocalDateTime initialDate = LocalDateTime.now().plusDays(1)
                .withHour(9).withMinute(0);
        return bookSession(athleteId, therapistId, facilityId,
                initialDate, 60, "Initial Evaluation");
    }

    // UC9 — Cancel Session
    public void cancelSession(int sessionId) {
        Optional<Session> sessionOpt =
                sessionRepository.findById(sessionId);
        sessionOpt.ifPresent(session -> {
            session.updateStatus(SessionStatus.CANCELLED);
            sessionRepository.updateStatus(sessionId,
                    SessionStatus.CANCELLED);
            // UC21 — Observer notification
            notificationEngine.notifyAllObservers(
                    "Session ID:" + sessionId + " has been CANCELLED.");
        });
    }

    // UC9 — Reschedule Session
    public void rescheduleSession(int sessionId,
                                  LocalDateTime newDate) {
        Optional<Session> sessionOpt =
                sessionRepository.findById(sessionId);
        sessionOpt.ifPresent(session -> {
            session.setSessionDate(newDate);
            sessionRepository.updateSession(session);
            // UC21 — Observer notification
            notificationEngine.notifyAllObservers(
                    "Session ID:" + sessionId +
                    " rescheduled to " + newDate);
        });
    }

    // UC13 — Update Session Status
    public void updateStatus(int sessionId, SessionStatus newStatus) {
        Optional<Session> sessionOpt =
                sessionRepository.findById(sessionId);
        sessionOpt.ifPresent(session -> {
            session.updateStatus(newStatus);
            sessionRepository.updateStatus(sessionId, newStatus);
            // UC21 — Observer notification
            notificationEngine.notifyAllObservers(
                    "Session ID:" + sessionId +
                    " status updated to " + newStatus.name());
        });
    }

    // UC5 — View Today's Sessions
    public List<Session> getTodaySessions() {
        return sessionRepository.findByDate(LocalDate.now());
    }

    // UC11 — View Daily Roster by Therapist
    public List<Session> getTodayRosterForTherapist(int therapistId) {
        return sessionRepository.findByTherapistId(therapistId)
                .stream()
                .filter(s -> s.getSessionDate().toLocalDate()
                        .equals(LocalDate.now()))
                .toList();
    }

    // UC8 — View Session History
    public List<Session> getSessionHistory(int athleteId) {
        return sessionRepository.findByAthleteId(athleteId);
    }

    // UC9 — View upcoming sessions
    public List<Session> getUpcomingSessions(int athleteId) {
        return sessionRepository.findUpcomingByAthleteId(athleteId);
    }

    public Optional<Session> findById(int sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public void cancelInitialSession(int athleteId) {
        sessionRepository.findUpcomingByAthleteId(athleteId)
                .stream()
                .filter(s -> s.getSessionType()
                        .equals("Initial Evaluation"))
                .findFirst()
                .ifPresent(s -> cancelSession(s.getSessionId()));
    }
}
