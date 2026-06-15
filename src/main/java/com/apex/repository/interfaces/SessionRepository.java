package com.apex.repository.interfaces;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.apex.domain.Session;
import com.apex.domain.SessionStatus;

// ISP: Only scheduling operations
public interface SessionRepository {
    void save(Session session);
    Optional<Session> findById(int sessionId);
    List<Session> findByAthleteId(int athleteId);
    List<Session> findByTherapistId(int therapistId);
    List<Session> findByDate(LocalDate date);
    List<Session> findUpcomingByAthleteId(int athleteId);
    List<Session> findByStatus(SessionStatus status);
    void updateStatus(int sessionId, SessionStatus status);
    void updateSession(Session session);
    void delete(int sessionId);
}
