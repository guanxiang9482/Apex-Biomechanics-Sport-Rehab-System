package com.apex.controller;

import com.apex.domain.*;
import com.apex.repository.interfaces.BiomechanicsRepository;
import com.apex.repository.interfaces.InvoiceRepository;
import com.apex.service.PaymentService;
import com.apex.service.ProfileService;
import com.apex.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handler Tier — Athlete endpoints
 * RBAC: ATHLETE and ADMIN roles only.
 * UC5, UC6, UC7, UC8, UC9, UC10, UC20
 */
@RestController
@RequestMapping("/api/athlete")
public class AthleteController {

    private final ProfileService profileService;
    private final SessionService sessionService;
    private final BiomechanicsRepository biomechanicsRepository;
    private final PaymentService paymentService;

    public AthleteController(ProfileService profileService,
                             SessionService sessionService,
                             BiomechanicsRepository biomechanicsRepository,
                             PaymentService paymentService) {
        this.profileService          = profileService;
        this.sessionService          = sessionService;
        this.biomechanicsRepository  = biomechanicsRepository;
        this.paymentService          = paymentService;
    }

    // UC6 — View Profile
    @GetMapping("/{athleteId}/profile")
    public ResponseEntity<?> getProfile(@PathVariable int athleteId) {
        Optional<Athlete> athlete = profileService.getProfile(athleteId);
        return athlete.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UC6 — Update Profile
    @PutMapping("/{athleteId}/profile")
    public ResponseEntity<?> updateProfile(
            @PathVariable int athleteId,
            @RequestBody Map<String, Object> body) {
        Optional<Athlete> athleteOpt =
                profileService.getProfile(athleteId);
        if (athleteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Athlete athlete = athleteOpt.get();
        if (body.containsKey("fullName"))
            athlete.setFullName((String) body.get("fullName"));
        if (body.containsKey("phone"))
            athlete.setPhone((String) body.get("phone"));
        if (body.containsKey("injuryStatus"))
            athlete.setInjuryStatus((String) body.get("injuryStatus"));
        if (body.containsKey("bodyWeightKg"))
            athlete.setBodyWeightKg(
                    ((Number) body.get("bodyWeightKg")).doubleValue());
        if (body.containsKey("heightCm"))
            athlete.setHeightCm(
                    ((Number) body.get("heightCm")).doubleValue());
        if (body.containsKey("postureNotes"))
            athlete.setPostureNotes((String) body.get("postureNotes"));

        profileService.updateProfile(athlete);
        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully"));
    }

    // UC5 — View Today's Sessions
    @GetMapping("/sessions/today")
    public ResponseEntity<List<Session>> getTodaySessions() {
        return ResponseEntity.ok(sessionService.getTodaySessions());
    }

    // UC7 — Book Rehab Session
    @PostMapping("/sessions/book")
    public ResponseEntity<?> bookSession(
            @RequestBody Map<String, Object> body) {
        try {
            int athleteId   = (Integer) body.get("athleteId");
            int therapistId = (Integer) body.get("therapistId");
            int facilityId  = (Integer) body.get("facilityId");
            String dateStr  = (String) body.get("sessionDate");
            int duration    = (Integer) body.get("durationMins");
            String type     = (String) body.get("sessionType");

            LocalDateTime sessionDate =
                    LocalDateTime.parse(dateStr);

            Session session = sessionService.bookSession(
                    athleteId, therapistId, facilityId,
                    sessionDate, duration, type);

            return ResponseEntity.ok(Map.of(
                    "message",   "Session booked successfully",
                    "sessionId", session.getSessionId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC8 — View Session History
    @GetMapping("/{athleteId}/sessions/history")
    public ResponseEntity<List<Session>> getSessionHistory(
            @PathVariable int athleteId) {
        return ResponseEntity.ok(
                sessionService.getSessionHistory(athleteId));
    }

    // UC9 — View Upcoming Sessions
    @GetMapping("/{athleteId}/sessions/upcoming")
    public ResponseEntity<List<Session>> getUpcomingSessions(
            @PathVariable int athleteId) {
        return ResponseEntity.ok(
                sessionService.getUpcomingSessions(athleteId));
    }

    // UC9 — Cancel Session
    @PutMapping("/sessions/{sessionId}/cancel")
    public ResponseEntity<?> cancelSession(
            @PathVariable int sessionId) {
        sessionService.cancelSession(sessionId);
        return ResponseEntity.ok(Map.of(
                "message", "Session cancelled successfully"));
    }

    // UC9 — Reschedule Session
    @PutMapping("/sessions/{sessionId}/reschedule")
    public ResponseEntity<?> rescheduleSession(
            @PathVariable int sessionId,
            @RequestBody Map<String, String> body) {
        LocalDateTime newDate =
                LocalDateTime.parse(body.get("newDate"));
        sessionService.rescheduleSession(sessionId, newDate);
        return ResponseEntity.ok(Map.of(
                "message", "Session rescheduled successfully"));
    }

    // UC10 — View Recovery Metrics (read-only)
    @GetMapping("/{athleteId}/recovery-metrics")
    public ResponseEntity<List<BiomechanicalRecord>> getRecoveryMetrics(
            @PathVariable int athleteId) {
        return ResponseEntity.ok(
                biomechanicsRepository.findByAthleteId(athleteId));
    }

    // UC20 — View own financial records
    @GetMapping("/{athleteId}/invoices")
    public ResponseEntity<List<Invoice>> getMyInvoices(
            @PathVariable int athleteId) {
        return ResponseEntity.ok(
                paymentService.getAthleteInvoices(athleteId));
    }
}
