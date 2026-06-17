package com.apex.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.apex.domain.FacilityStatus;
import com.apex.domain.Session;
import com.apex.domain.SessionStatus;
import com.apex.repository.interfaces.AthleteRepository;
import com.apex.repository.interfaces.FacilityRepository;
import com.apex.repository.interfaces.PhysiotherapistRepository;
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

    private static final ZoneId APP_ZONE =
            ZoneId.of("Asia/Kuala_Lumpur");

    private final SessionRepository sessionRepository;
    private final NotificationEngine notificationEngine;
    private final FacilityRepository facilityRepository;
    private final AthleteRepository athleteRepository;
    private final PhysiotherapistRepository physiotherapistRepository;

    public SessionService(SessionRepository sessionRepository,
                          NotificationEngine notificationEngine,
                          FacilityRepository facilityRepository,
                          AthleteRepository athleteRepository,
                          PhysiotherapistRepository physiotherapistRepository) {
        this.sessionRepository  = sessionRepository;
        this.notificationEngine = notificationEngine;
        this.facilityRepository = facilityRepository;
        this.athleteRepository  = athleteRepository;
        this.physiotherapistRepository = physiotherapistRepository;
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

        notifySessionParticipants(session,
                "Session booked: " + sessionType + " on " + scheduledDate);
        return session;
    }

    // Used by Facade for initial evaluation (UC15)
    public Session scheduleInitialSession(int athleteId,
                                          int therapistId,
                                          int facilityId) {
        LocalDateTime initial = now()
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
            notifySessionParticipants(s,
                    "Session #" + sessionId + " has been cancelled.");
        });
    }

    public void cancelSessionForAthlete(int sessionId, int athleteId) {
        Session session = getSessionOwnedByAthlete(sessionId, athleteId);
        session.updateStatus(SessionStatus.CANCELLED);
        sessionRepository.updateStatus(sessionId, SessionStatus.CANCELLED);
        notifySessionParticipants(session,
                "Session #" + sessionId + " has been cancelled.");
    }

    // UC9 — Reschedule Session
    public void rescheduleSession(int sessionId,
                                   LocalDateTime newDate) {
        sessionRepository.findById(sessionId).ifPresent(s -> {
            ensureSlotCanBeBooked(s.getTherapistId(), s.getFacilityId(),
                    newDate, s.getDurationMins(), sessionId);
            s.setSessionDate(newDate);
            sessionRepository.updateSession(s);
            notifySessionParticipants(s,
                    "Session #" + sessionId +
                    " rescheduled to " + newDate);
        });
    }

    public void rescheduleSessionForAthlete(int sessionId, int athleteId,
                                            LocalDateTime newDate) {
        Session session = getSessionOwnedByAthlete(sessionId, athleteId);
        ensureSlotCanBeBooked(session.getTherapistId(), session.getFacilityId(),
                newDate, session.getDurationMins(), sessionId);
        session.setSessionDate(newDate);
        sessionRepository.updateSession(session);
        notifySessionParticipants(session,
                "Session #" + sessionId + " rescheduled to " + newDate);
    }

    // UC13 — Update Session Status
    public void updateStatus(int sessionId,
                              SessionStatus newStatus) {
        sessionRepository.findById(sessionId).ifPresent(s -> {
            s.updateStatus(newStatus);
            sessionRepository.updateStatus(sessionId, newStatus);
            notifySessionParticipants(s,
                    "Session #" + sessionId +
                    " status updated to " + newStatus.name());
        });
    }

    public void updateStatusForTherapist(int sessionId, int therapistId,
                                         SessionStatus newStatus) {
        Session session = getSessionAssignedToTherapist(sessionId, therapistId);
        session.updateStatus(newStatus);
        sessionRepository.updateStatus(sessionId, newStatus);
        notifySessionParticipants(session,
                "Session #" + sessionId +
                " status updated to " + newStatus.name());
    }

    // UC5 — Today's sessions
    public List<Session> getTodaySessions() {
        return sessionRepository.findByDate(today());
    }

    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    public List<Session> getTodaySessionsForAthlete(int athleteId) {
        return sessionRepository.findByAthleteId(athleteId)
                .stream()
                .filter(s -> s.getSessionDate().toLocalDate()
                        .equals(today()))
                .toList();
    }

    // UC11 — Therapist daily roster
    public List<Session> getTodayRosterForTherapist(
            int therapistId) {
        return sessionRepository.findByTherapistId(therapistId)
                .stream()
                .filter(s -> s.getSessionDate().toLocalDate()
                        .equals(today()))
                .toList();
    }

    public List<Session> getSessionsForTherapist(int therapistId) {
        return sessionRepository.findByTherapistId(therapistId);
    }

    public List<Session> getCompletedSessions() {
        return sessionRepository.findByStatus(SessionStatus.COMPLETED);
    }

    public List<Session> getSessionsByStatus(SessionStatus status) {
        return sessionRepository.findByStatus(status);
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

    public Session getSessionOwnedByAthlete(int sessionId, int athleteId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Session not found."));
        if (session.getAthleteId() != athleteId) {
            throw new IllegalArgumentException(
                    "This session does not belong to the selected athlete.");
        }
        return session;
    }

    public Session getSessionAssignedToTherapist(int sessionId,
                                                 int therapistId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Session not found."));
        if (session.getTherapistId() != therapistId) {
            throw new IllegalArgumentException(
                    "This session is not assigned to the selected therapist.");
        }
        return session;
    }

    public List<LocalDateTime> getAvailableSlots(int therapistId,
                                                 int facilityId,
                                                 LocalDate date,
                                                 int durationMins) {
        return getAvailableSlots(therapistId, facilityId, date,
                durationMins, null);
    }

    public List<LocalDateTime> getAvailableSlots(int therapistId,
                                                 int facilityId,
                                                 LocalDate date,
                                                 int durationMins,
                                                 Integer currentSessionId) {
        validateFacility(facilityId);

        return IntStream.rangeClosed(9, 16)
                .mapToObj(hour -> LocalDateTime.of(date,
                        LocalTime.of(hour, 0)))
                .filter(slot -> slot.isAfter(now()))
                .filter(slot -> !hasBookingConflict(therapistId,
                        facilityId, slot, durationMins, currentSessionId))
                .toList();
    }

    private LocalDate today() {
        return LocalDate.now(APP_ZONE);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(APP_ZONE);
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

    private void notifySessionParticipants(Session session, String event) {
        athleteRepository.findById(session.getAthleteId())
                .ifPresent(athlete -> notificationEngine.notifyObserver(
                        athlete.getUserId(), event));
        physiotherapistRepository.findById(session.getTherapistId())
                .ifPresent(therapist -> notificationEngine.notifyObserver(
                        therapist.getUserId(), event));
    }
}
