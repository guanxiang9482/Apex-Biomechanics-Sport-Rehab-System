package com.apex.controller;

import com.apex.domain.*;
import com.apex.repository.interfaces.BiomechanicsRepository;
import com.apex.repository.interfaces.MedicalRecordRepository;
import com.apex.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handler Tier — Physiotherapist endpoints
 * RBAC: THERAPIST and ADMIN roles only.
 * UC11, UC12, UC13, UC14
 */
@RestController
@RequestMapping("/api/therapist")
public class TherapistController {

    private final SessionService sessionService;
    private final BiomechanicsRepository biomechanicsRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public TherapistController(
            SessionService sessionService,
            BiomechanicsRepository biomechanicsRepository,
            MedicalRecordRepository medicalRecordRepository) {
        this.sessionService          = sessionService;
        this.biomechanicsRepository  = biomechanicsRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    // UC11 — View Daily Roster & Labs
    @GetMapping("/{therapistId}/roster/today")
    public ResponseEntity<List<Session>> getTodayRoster(
            @PathVariable int therapistId) {
        return ResponseEntity.ok(
                sessionService.getTodayRosterForTherapist(therapistId));
    }

    // UC12 — Log Biomechanical Data
    @PostMapping("/biomechanics/log")
    public ResponseEntity<?> logBiomechanicalData(
            @RequestBody Map<String, Object> body) {
        try {
            int sessionId = (Integer) body.get("sessionId");
            double jumpPower =
                    ((Number) body.get("jumpPower")).doubleValue();
            double jointMobility =
                    ((Number) body.get("jointMobility")).doubleValue();
            double postureScore =
                    ((Number) body.get("postureScore")).doubleValue();
            String notes = (String) body.getOrDefault("notes", "");

            BiomechanicalRecord record = new BiomechanicalRecord(
                    sessionId, jumpPower, jointMobility,
                    postureScore, notes);
            biomechanicsRepository.save(record);

            return ResponseEntity.ok(Map.of(
                    "message",  "Biomechanical data logged successfully",
                    "recordId", record.getRecordId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC13 — Update Session Status
    @PutMapping("/sessions/{sessionId}/status")
    public ResponseEntity<?> updateSessionStatus(
            @PathVariable int sessionId,
            @RequestBody Map<String, String> body) {
        try {
            SessionStatus status =
                    SessionStatus.valueOf(body.get("status"));
            sessionService.updateStatus(sessionId, status);
            return ResponseEntity.ok(Map.of(
                    "message", "Session status updated to " +
                               status.name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid status value"));
        }
    }

    // UC14 — Generate Athlete Report
    @PostMapping("/reports/generate")
    public ResponseEntity<?> generateReport(
            @RequestBody Map<String, Object> body) {
        int athleteId   = (Integer) body.get("athleteId");
        int therapistId = (Integer) body.get("therapistId");
        String summary  = (String) body.get("summary");

        ClinicalReport report = new ClinicalReport(
                athleteId, therapistId, summary);
        medicalRecordRepository.saveReport(report);

        return ResponseEntity.ok(Map.of(
                "message",  "Report generated successfully",
                "reportId", report.getReportId()
        ));
    }

    // UC14 — View Athlete Reports
    @GetMapping("/reports/{athleteId}")
    public ResponseEntity<List<ClinicalReport>> getAthleteReports(
            @PathVariable int athleteId) {
        return ResponseEntity.ok(
                medicalRecordRepository
                        .findReportsByAthleteId(athleteId));
    }

    // UC12 — View biomechanical records by session
    @GetMapping("/biomechanics/session/{sessionId}")
    public ResponseEntity<List<BiomechanicalRecord>> getBySession(
            @PathVariable int sessionId) {
        return ResponseEntity.ok(
                biomechanicsRepository.findBySessionId(sessionId));
    }
}
