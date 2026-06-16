package com.apex.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.apex.domain.FacilityStatus;
import com.apex.domain.Session;
import com.apex.domain.SessionStatus;
import com.apex.repository.interfaces.FacilityRepository;
import com.apex.repository.interfaces.SessionRepository;
import com.apex.service.observer.NotificationEngine;

/**
 * SRP: Responsible for rehab session scheduling
 * and lifecycle management.
 * UC5, UC7, UC9, UC11, UC13
 *
 * Fix 6: getSessionHistory() now returns only COMPLETED
 * sessions, matching UC8 spec ("past medical/rehab interactions").
 * getCompletedSessionHistory() is a new named method used by
 * TherapistController UC14 report compilation.
 */
@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final NotificationEngine notificationEngine;
    private final FacilityRepository facilityRepository;

    public SessionService(SessionRepository sessionRepository,
                          NotificationEngine notificationEngine,
                          FacilityRepository facilityRepository) {
        this.sessionRepository  = sessionRepository;
        this.notificationEngine = notificationEngine;
        this.facilityRepository = facilityRepository;
    }

    // UC7 — Book Rehab Session
    public Session bookSession(int athleteId, int therapistId,
                               int facilityId,
                               LocalDateTime scheduledDate,
                               int durationMins,
                               String sessionType) {
        ensureSlotCanBeBooked(therapistId, facilityId,
                scheduledDate, durationMins, null);

        Session session = new Session(athleteId, therapistId,
                facilityId, scheduledDate, durationMins, sessionType);
        sessionRepository.save(session);

        // UC21 — Observer notification (broadcast)
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
            ensureSlotCanBeBooked(s.getTherapistId(), s.getFacilityId(),
                    newDate, s.getDurationMins(), sessionId);
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

    /**
     * UC8 — View Session History
     *
     * Fix 6: Returns only COMPLETED sessions.
     * The proposal states UC8 displays "past medical/rehab
     * interactions" — scheduled or cancelled sessions are
     * not part of the athlete's clinical history.
     */
    public List<Session> getSessionHistory(int athleteId) {
        return sessionRepository.findByAthleteId(athleteId)
                .stream()
                .filter(s -> s.getStatus()
                        == SessionStatus.COMPLETED)
                .toList();
    }

    /**
     * UC14 — Used by TherapistController to compile
     * athlete report. Same logic as UC8 history.
     */
    public List<Session> getCompletedSessionHistory(int athleteId) {
        return getSessionHistory(athleteId);
    }

    // UC9 — Upcoming sessions
    public List<Session> getUpcomingSessions(int athleteId) {
        return sessionRepository.findUpcomingByAthleteId(athleteId);
    }

    public Optional<Session> findById(int sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public List<LocalDateTime> getAvailableSlots(int therapistId,
                                                 int facilityId,
                                                 LocalDate date,
                                                 int durationMins) {
        validateFacility(facilityId);

        return IntStream.rangeClosed(9, 16)
                .mapToObj(hour -> LocalDateTime.of(date,
                        LocalTime.of(hour, 0)))
                .filter(slot -> slot.isAfter(LocalDateTime.now()))
                .filter(slot -> !hasBookingConflict(therapistId,
                        facilityId, slot, durationMins, null))
                .toList();
    }

    public void cancelInitialSession(int athleteId) {
        sessionRepository.findUpcomingByAthleteId(athleteId)
                .stream()
                .filter(s -> "Initial Evaluation"
                        .equals(s.getSessionType()))
                .findFirst()
                .ifPresent(s -> cancelSession(s.getSessionId()));
    }

    private void ensureSlotCanBeBooked(int therapistId, int facilityId,
                                       LocalDateTime start,
                                       int durationMins,
                                       Integer currentSessionId) {
        validateFacility(facilityId);
        if (hasBookingConflict(therapistId, facilityId, start,
                durationMins, currentSessionId)) {
            throw new IllegalArgumentException(
                    "Selected therapist or facility is not available at this time.");
        }
    }

    private void validateFacility(int facilityId) {
        var facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Facility not found."));
        if (facility.getStatus() != FacilityStatus.AVAILABLE) {
            throw new IllegalArgumentException(
                    "Selected facility is not available.");
        }
    }

    private boolean hasBookingConflict(int therapistId, int facilityId,
                                       LocalDateTime start,
                                       int durationMins,
                                       Integer currentSessionId) {
        LocalDateTime end = start.plusMinutes(durationMins);

        return sessionRepository.findByDate(start.toLocalDate())
                .stream()
                .filter(session -> currentSessionId == null
                        || session.getSessionId() != currentSessionId)
                .filter(session -> session.getStatus()
                        != SessionStatus.CANCELLED)
                .filter(session -> session.getTherapistId() == therapistId
                        || session.getFacilityId() == facilityId)
                .anyMatch(session -> {
                    LocalDateTime existingStart = session.getSessionDate();
                    LocalDateTime existingEnd = existingStart.plusMinutes(
                            session.getDurationMins());
                    return start.isBefore(existingEnd)
                            && existingStart.isBefore(end);
                });
    }
}
