package com.apex.controller;

import com.apex.domain.*;
import com.apex.repository.interfaces.BiomechanicsRepository;
import com.apex.repository.interfaces.ClinicalReportRepository;
import com.apex.repository.interfaces.MedicalRecordRepository;
import com.apex.repository.interfaces.PhysiotherapistRepository;
import com.apex.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UC11, UC12, UC13, UC14
 * All physiotherapist clinical operations.
 */
@RestController
@RequestMapping("/api/therapist")
public class TherapistController {

    private final SessionService sessionService;
    private final BiomechanicsRepository biomechanicsRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ClinicalReportRepository clinicalReportRepository;
    private final PhysiotherapistRepository physiotherapistRepository;

    public TherapistController(
            SessionService sessionService,
            BiomechanicsRepository biomechanicsRepository,
            MedicalRecordRepository medicalRecordRepository,
            ClinicalReportRepository clinicalReportRepository,
            PhysiotherapistRepository physiotherapistRepository) {
        this.sessionService           = sessionService;
        this.biomechanicsRepository   = biomechanicsRepository;
        this.medicalRecordRepository  = medicalRecordRepository;
        this.clinicalReportRepository = clinicalReportRepository;
        this.physiotherapistRepository = physiotherapistRepository;
    }

    @GetMapping("/user/{userId}/profile")
    public ResponseEntity<?> getProfileByUserId(
            @PathVariable int userId) {
        return physiotherapistRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UC11 — Today's roster
    @GetMapping("/{therapistId}/roster/today")
    public ResponseEntity<List<Session>> getTodayRoster(
            @PathVariable int therapistId) {
        return ResponseEntity.ok(
                sessionService.getTodayRosterForTherapist(
                        therapistId));
    }

    // UC12 — Log biomechanical data
    @PostMapping("/biomechanics/log")
    public ResponseEntity<?> logBiomechanicalData(
            @RequestBody Map<String, Object> body) {
        try {
            int athleteId   = (Integer) body.get("athleteId");
            int therapistId = (Integer) body.get("therapistId");
            int sessionId   = (Integer) body.get("sessionId");
            double jumpPower =
                    ((Number) body.get("jumpPower")).doubleValue();
            double jointMobility =
                    ((Number) body.get("jointMobility")).doubleValue();
            double postureScore =
                    ((Number) body.get("postureScore")).doubleValue();
            String note =
                    (String) body.getOrDefault("treatmentNote", "");

            BiomechanicalRecord record = new BiomechanicalRecord(
                    athleteId, therapistId, sessionId,
                    jumpPower, jointMobility, postureScore, note);
            biomechanicsRepository.save(record);

            return ResponseEntity.ok(Map.of(
                    "message",  "Biomechanical data logged",
                    "recordId", record.getRecordId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC12 — View records by session
    @GetMapping("/biomechanics/session/{sessionId}")
    public ResponseEntity<List<BiomechanicalRecord>> getBySession(
            @PathVariable int sessionId) {
        return ResponseEntity.ok(
                biomechanicsRepository.findBySessionId(sessionId));
    }

    // UC13 — Update session status
    @PutMapping("/sessions/{sessionId}/status")
    public ResponseEntity<?> updateSessionStatus(
            @PathVariable int sessionId,
            @RequestBody Map<String, String> body) {
        try {
            SessionStatus status =
                    SessionStatus.valueOf(body.get("status"));
            sessionService.updateStatus(sessionId, status);
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Session status updated to " + status.name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid status value"));
        }
    }

    // UC14 — Create medical record
    @PostMapping("/medical-records/create")
    public ResponseEntity<?> createMedicalRecord(
            @RequestBody Map<String, Object> body) {
        int athleteId   = (Integer) body.get("athleteId");
        int therapistId = (Integer) body.get("therapistId");
        String notes    = (String) body.get("diagnosisNotes");

        MedicalRecord record = new MedicalRecord(
                athleteId, therapistId, notes);
        medicalRecordRepository.save(record);

        return ResponseEntity.ok(Map.of(
                "message",  "Medical record created",
                "recordId", record.getRecordId()
        ));
    }

    // UC14 — View athlete medical records
    @GetMapping("/medical-records/{athleteId}")
    public ResponseEntity<List<MedicalRecord>> getMedicalRecords(
            @PathVariable int athleteId) {
        return ResponseEntity.ok(
                medicalRecordRepository.findByAthleteId(athleteId));
    }

    // UC14 — Generate clinical report
    @PostMapping("/reports/generate")
    public ResponseEntity<?> generateReport(
            @RequestBody Map<String, Object> body) {
        int therapistId  = (Integer) body.get("therapistId");
        String reportType= (String) body.get("reportType");
        String description=(String) body.get("description");

        ClinicalReport report = new ClinicalReport(
                therapistId, reportType, description);
        clinicalReportRepository.save(report);

        return ResponseEntity.ok(Map.of(
                "message",  "Report generated successfully",
                "reportId", report.getReportId()
        ));
    }

    // UC14 — View own reports
    @GetMapping("/reports/{therapistId}")
    public ResponseEntity<List<ClinicalReport>> getReports(
            @PathVariable int therapistId) {
        return ResponseEntity.ok(
                clinicalReportRepository
                        .findByTherapistId(therapistId));
    }
}
