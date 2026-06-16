package com.apex.controller;

import com.apex.domain.*;
import com.apex.repository.interfaces.BiomechanicsRepository;
import com.apex.repository.interfaces.FacilityRepository;
import com.apex.repository.interfaces.PhysiotherapistRepository;
import com.apex.service.PaymentService;
import com.apex.service.ProfileService;
import com.apex.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * UC5, UC6, UC7, UC8, UC9, UC10, UC20
 * All athlete self-service operations.
 */
@RestController
@RequestMapping("/api/athlete")
public class AthleteController {

    private final ProfileService profileService;
    private final SessionService sessionService;
    private final BiomechanicsRepository biomechanicsRepository;
    private final PaymentService paymentService;
    private final FacilityRepository facilityRepository;
    private final PhysiotherapistRepository physiotherapistRepository;

    public AthleteController(ProfileService profileService,
                             SessionService sessionService,
                             BiomechanicsRepository biomechanicsRepository,
                             PaymentService paymentService,
                             FacilityRepository facilityRepository,
                             PhysiotherapistRepository physiotherapistRepository) {
        this.profileService         = profileService;
        this.sessionService         = sessionService;
        this.biomechanicsRepository = biomechanicsRepository;
        this.paymentService         = paymentService;
        this.facilityRepository     = facilityRepository;
        this.physiotherapistRepository = physiotherapistRepository;
    }

    // UC6 — Get profile by athleteId
    @GetMapping("/{athleteId}/profile")
    public ResponseEntity<?> getProfile(
            @PathVariable int athleteId) {
        Optional<Athlete> athlete =
                profileService.getProfile(athleteId);
        return athlete.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UC6 — Get profile by userId (used after login)
    @GetMapping("/user/{userId}/profile")
    public ResponseEntity<?> getProfileByUserId(
            @PathVariable int userId) {
        Optional<Athlete> athlete =
                profileService.getProfileByUserId(userId);
        return athlete.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UC6 — Update profile
    @PutMapping("/{athleteId}/profile")
    public ResponseEntity<?> updateProfile(
            @PathVariable int athleteId,
            @RequestBody Map<String, Object> body) {
        Optional<Athlete> athleteOpt =
                profileService.getProfile(athleteId);
        if (athleteOpt.isEmpty())
            return ResponseEntity.notFound().build();

        Athlete athlete = athleteOpt.get();
        if (body.containsKey("injuryStatus"))
            athlete.setInjuryStatus(
                    (String) body.get("injuryStatus"));
        if (body.containsKey("sport"))
            athlete.setSport((String) body.get("sport"));
        if (body.containsKey("bodyWeightKg"))
            athlete.setBodyWeightKg(
                    ((Number) body.get("bodyWeightKg"))
                            .doubleValue());
        if (body.containsKey("heightCm"))
            athlete.setHeightCm(
                    ((Number) body.get("heightCm"))
                            .doubleValue());

        profileService.updateProfile(athlete);
        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "bmi", athlete.getBmi()
        ));
    }

    // UC5 — Today's sessions
    @GetMapping("/sessions/today")
    public ResponseEntity<List<Session>> getTodaySessions() {
        return ResponseEntity.ok(
                sessionService.getTodaySessions());
    }

    @GetMapping("/therapists")
    public ResponseEntity<List<Physiotherapist>> getTherapists() {
        return ResponseEntity.ok(
                physiotherapistRepository.findAll());
    }

    @GetMapping("/facilities")
    public ResponseEntity<List<Facility>> getFacilities() {
        return ResponseEntity.ok(
                facilityRepository.findAll());
    }

    @GetMapping("/sessions/availability")
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam int therapistId,
            @RequestParam int facilityId,
            @RequestParam String date,
            @RequestParam(defaultValue = "60") int durationMins) {
        try {
            return ResponseEntity.ok(Map.of(
                    "availableSlots",
                    sessionService.getAvailableSlots(
                            therapistId, facilityId,
                            LocalDate.parse(date), durationMins)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC7 — Book session
    @PostMapping("/sessions/book")
    public ResponseEntity<?> bookSession(
            @RequestBody Map<String, Object> body) {
        try {
            int athleteId   = (Integer) body.get("athleteId");
            int therapistId = (Integer) body.get("therapistId");
            int facilityId  = (Integer) body.get("facilityId");
            String dateStr  = (String) body.get("sessionDate");
            int duration    = body.containsKey("durationMins")
                    ? (Integer) body.get("durationMins") : 60;
            String type     = (String) body.get("sessionType");

            Session session = sessionService.bookSession(
                    athleteId, therapistId, facilityId,
                    LocalDateTime.parse(dateStr), duration, type);

            return ResponseEntity.ok(Map.of(
                    "message",   "Session booked successfully",
                    "sessionId", session.getSessionId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC8 — Session history
    @GetMapping("/{athleteId}/sessions/history")
    public ResponseEntity<List<Session>> getSessionHistory(
            @PathVariable int athleteId) {
        return ResponseEntity.ok(
                sessionService.getSessionHistory(athleteId));
    }

    // UC9 — Upcoming sessions
    @GetMapping("/{athleteId}/sessions/upcoming")
    public ResponseEntity<List<Session>> getUpcomingSessions(
            @PathVariable int athleteId) {
        return ResponseEntity.ok(
                sessionService.getUpcomingSessions(athleteId));
    }

    // UC9 — Cancel session
    @PutMapping("/sessions/{sessionId}/cancel")
    public ResponseEntity<?> cancelSession(
            @PathVariable int sessionId) {
        sessionService.cancelSession(sessionId);
        return ResponseEntity.ok(Map.of(
                "message", "Session cancelled successfully"));
    }

    // UC9 — Reschedule session
    @PutMapping("/sessions/{sessionId}/reschedule")
    public ResponseEntity<?> rescheduleSession(
            @PathVariable int sessionId,
            @RequestBody Map<String, String> body) {
        sessionService.rescheduleSession(sessionId,
                LocalDateTime.parse(body.get("newDate")));
        return ResponseEntity.ok(Map.of(
                "message", "Session rescheduled successfully"));
    }

    // UC10 — Recovery metrics
    @GetMapping("/{athleteId}/recovery-metrics")
    public ResponseEntity<List<BiomechanicalRecord>>
    getRecoveryMetrics(@PathVariable int athleteId) {
        return ResponseEntity.ok(
                biomechanicsRepository.findByAthleteId(athleteId));
    }

    // UC20 — Own invoices
    @GetMapping("/{athleteId}/invoices")
    public ResponseEntity<List<Invoice>> getMyInvoices(
            @PathVariable int athleteId) {
        return ResponseEntity.ok(
                paymentService.getAthleteInvoices(athleteId));
    }
}
