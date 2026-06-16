package com.apex.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apex.domain.Athlete;
import com.apex.domain.BiomechanicalRecord;
import com.apex.domain.ClinicalReport;
import com.apex.domain.MedicalRecord;
import com.apex.domain.Session;
import com.apex.domain.SessionStatus;
import com.apex.repository.interfaces.BiomechanicsRepository;
import com.apex.repository.interfaces.ClinicalReportRepository;
import com.apex.repository.interfaces.MedicalRecordRepository;
import com.apex.repository.interfaces.PhysiotherapistRepository;
import com.apex.service.ProfileService;
import com.apex.service.SessionService;

/**
 * UC11, UC12, UC13, UC14
 * All physiotherapist clinical operations.
 *
 * Fix 4: UC14 generateReport now:
 *   - Accepts athleteId in request body
 *   - Compiles sessions, biomechanical records, medical records
 *   - Returns structured progress report
 *   - New endpoint /reports/athlete/{athleteId}/compile for
 *     read-only preview without saving to DB
 */
@RestController
@RequestMapping("/api/therapist")
public class TherapistController {

    private final SessionService sessionService;
    private final BiomechanicsRepository biomechanicsRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ClinicalReportRepository clinicalReportRepository;
    private final PhysiotherapistRepository physiotherapistRepository;
    private final ProfileService profileService;

    public TherapistController(
            SessionService sessionService,
            BiomechanicsRepository biomechanicsRepository,
            MedicalRecordRepository medicalRecordRepository,
            ClinicalReportRepository clinicalReportRepository,
            PhysiotherapistRepository physiotherapistRepository,
            ProfileService profileService) {
        this.sessionService           = sessionService;
        this.biomechanicsRepository   = biomechanicsRepository;
        this.medicalRecordRepository  = medicalRecordRepository;
        this.clinicalReportRepository = clinicalReportRepository;
        this.physiotherapistRepository = physiotherapistRepository;
        this.profileService           = profileService;
    }

    @GetMapping("/user/{userId}/profile")
    public ResponseEntity<?> getProfileByUserId(
            @PathVariable int userId,
            @RequestHeader("X-User-Id") int requestUserId) {
        if (userId != requestUserId) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "You can only access your own profile."));
        }
        return physiotherapistRepository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/athletes")
    public ResponseEntity<?> getAthletes(
            @RequestHeader("X-User-Id") int userId) {
        var therapist = physiotherapistRepository.findByUserId(userId);
        if (therapist.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Therapist profile not found for this user."));
        }
        return ResponseEntity.ok(getAssignedAthleteList(
                therapist.get().getTherapistId()));
    }

    @GetMapping("/{therapistId}/athletes")
    public ResponseEntity<?> getAssignedAthletes(
            @PathVariable int therapistId,
            @RequestHeader("X-User-Id") int userId) {
        ResponseEntity<?> accessError =
                requireOwnedTherapist(userId, therapistId);
        if (accessError != null) return accessError;
        return ResponseEntity.ok(getAssignedAthleteList(therapistId));
    }

    @GetMapping("/{therapistId}/sessions")
    public ResponseEntity<?> getTherapistSessions(
            @PathVariable int therapistId,
            @RequestHeader("X-User-Id") int userId) {
        ResponseEntity<?> accessError =
                requireOwnedTherapist(userId, therapistId);
        if (accessError != null) return accessError;
        return ResponseEntity.ok(
                sessionService.getSessionsForTherapist(therapistId));
    }

    // UC11 — Today's roster
    @GetMapping("/{therapistId}/roster/today")
    public ResponseEntity<?> getTodayRoster(
            @PathVariable int therapistId,
            @RequestHeader("X-User-Id") int userId) {
        ResponseEntity<?> accessError =
                requireOwnedTherapist(userId, therapistId);
        if (accessError != null) return accessError;
        return ResponseEntity.ok(
                sessionService.getTodayRosterForTherapist(
                        therapistId));
    }

    // UC12 — Log biomechanical data
    @PostMapping("/biomechanics/log")
    public ResponseEntity<?> logBiomechanicalData(
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-User-Id") int userId) {
        try {
            int athleteId   = (Integer) body.get("athleteId");
            int therapistId = (Integer) body.get("therapistId");
            int sessionId   = (Integer) body.get("sessionId");
            ResponseEntity<?> accessError =
                    requireOwnedTherapist(userId, therapistId);
            if (accessError != null) return accessError;
            Session session = sessionService
                    .getSessionAssignedToTherapist(sessionId, therapistId);
            if (session.getAthleteId() != athleteId) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error",
                                "Selected session does not belong to this athlete."));
            }
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
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC12 — View records by session
    @GetMapping("/biomechanics/session/{sessionId}")
    public ResponseEntity<?> getBySession(
            @PathVariable int sessionId,
            @RequestHeader("X-User-Id") int userId) {
        var therapist = physiotherapistRepository.findByUserId(userId);
        if (therapist.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Therapist profile not found for this user."));
        }
        sessionService.getSessionAssignedToTherapist(
                sessionId, therapist.get().getTherapistId());
        return ResponseEntity.ok(
                biomechanicsRepository.findBySessionId(sessionId));
    }

    // UC12 — View all records for an athlete (recovery metrics)
    @GetMapping("/biomechanics/athlete/{athleteId}")
    public ResponseEntity<?> getByAthlete(
            @PathVariable int athleteId,
            @RequestHeader("X-User-Id") int userId) {
        var therapist = physiotherapistRepository.findByUserId(userId);
        if (therapist.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Therapist profile not found for this user."));
        }
        ResponseEntity<?> accessError = requireAssignedAthlete(
                therapist.get().getTherapistId(), athleteId);
        if (accessError != null) return accessError;
        return ResponseEntity.ok(
                biomechanicsRepository.findByAthleteId(athleteId));
    }

    // UC13 — Update session status
    @PutMapping("/sessions/{sessionId}/status")
    public ResponseEntity<?> updateSessionStatus(
            @PathVariable int sessionId,
            @RequestBody Map<String, String> body,
            @RequestHeader("X-User-Id") int userId) {
        try {
            SessionStatus status =
                    SessionStatus.valueOf(body.get("status"));
            int therapistId = Integer.parseInt(body.get("therapistId"));
            ResponseEntity<?> accessError =
                    requireOwnedTherapist(userId, therapistId);
            if (accessError != null) return accessError;
            sessionService.updateStatusForTherapist(
                    sessionId, therapistId, status);
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Session status updated to " + status.name()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC14 — Create medical record
    @PostMapping("/medical-records/create")
    public ResponseEntity<?> createMedicalRecord(
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-User-Id") int userId) {
        int athleteId   = (Integer) body.get("athleteId");
        int therapistId = (Integer) body.get("therapistId");
        String notes    = (String) body.get("diagnosisNotes");
        ResponseEntity<?> therapistError =
                requireOwnedTherapist(userId, therapistId);
        if (therapistError != null) return therapistError;
        ResponseEntity<?> athleteError =
                requireAssignedAthlete(therapistId, athleteId);
        if (athleteError != null) return athleteError;

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
    public ResponseEntity<?> getMedicalRecords(
            @PathVariable int athleteId,
            @RequestHeader("X-User-Id") int userId) {
        var therapist = physiotherapistRepository.findByUserId(userId);
        if (therapist.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Therapist profile not found for this user."));
        }
        ResponseEntity<?> accessError = requireAssignedAthlete(
                therapist.get().getTherapistId(), athleteId);
        if (accessError != null) return accessError;
        return ResponseEntity.ok(
                medicalRecordRepository.findByAthleteId(athleteId));
    }

    /**
     * UC14 — Compile Athlete Progress Report (Read-only preview)
     *
     * Fix 4: Retrieves session history, biomechanical records,
     * and medical records for an athlete and compiles them into
     * a structured JSON report. This is the "Generate Report"
     * UC14 requires per the proposal.
     */
    @GetMapping("/reports/athlete/{athleteId}/compile")
    public ResponseEntity<?> compileAthleteReport(
            @PathVariable int athleteId,
            @RequestHeader("X-User-Id") int userId) {
        var therapist = physiotherapistRepository.findByUserId(userId);
        if (therapist.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Therapist profile not found for this user."));
        }
        ResponseEntity<?> accessError = requireAssignedAthlete(
                therapist.get().getTherapistId(), athleteId);
        if (accessError != null) return accessError;

        // Retrieve all clinical data for this athlete
        List<Session> completedSessions =
                sessionService.getCompletedSessionHistory(athleteId);
        List<BiomechanicalRecord> bioRecords =
                biomechanicsRepository.findByAthleteId(athleteId);
        List<MedicalRecord> medRecords =
                medicalRecordRepository.findByAthleteId(athleteId);

        // Compile summary statistics from biomechanical data
        double avgJumpPower = bioRecords.stream()
                .mapToDouble(BiomechanicalRecord::getJumpPower)
                .average().orElse(0.0);
        double avgJointMobility = bioRecords.stream()
                .mapToDouble(BiomechanicalRecord::getJointMobility)
                .average().orElse(0.0);
        double avgPostureScore = bioRecords.stream()
                .mapToDouble(BiomechanicalRecord::getPostureScore)
                .average().orElse(0.0);

        // Build structured report
        Map<String, Object> report = new HashMap<>();
        report.put("athleteId",          athleteId);
        report.put("generatedAt",        LocalDateTime.now().toString());
        report.put("completedSessions",  completedSessions);
        report.put("totalSessions",      completedSessions.size());
        report.put("biomechanicalRecords", bioRecords);
        report.put("totalBioRecords",    bioRecords.size());
        report.put("medicalRecords",     medRecords);
        report.put("avgJumpPower",
                Math.round(avgJumpPower * 100.0) / 100.0);
        report.put("avgJointMobility",
                Math.round(avgJointMobility * 100.0) / 100.0);
        report.put("avgPostureScore",
                Math.round(avgPostureScore * 100.0) / 100.0);

        return ResponseEntity.ok(report);
    }

    /**
     * UC14 — Save formal clinical report to DB
     *
     * Fix 4: Now accepts athleteId, compiles summary stats,
     * and stores a rich description instead of empty fields.
     */
    @PostMapping("/reports/generate")
    public ResponseEntity<?> generateReport(
            @RequestBody Map<String, Object> body,
            @RequestHeader("X-User-Id") int userId) {
        int therapistId   = (Integer) body.get("therapistId");
        int athleteId     = (Integer) body.get("athleteId");
        String reportType = (String) body.get("reportType");
        String extraNotes = (String) body.getOrDefault(
                "description", "");
        ResponseEntity<?> therapistError =
                requireOwnedTherapist(userId, therapistId);
        if (therapistError != null) return therapistError;
        ResponseEntity<?> athleteError =
                requireAssignedAthlete(therapistId, athleteId);
        if (athleteError != null) return athleteError;

        // Compile counts for rich description
        List<Session> sessions =
                sessionService.getCompletedSessionHistory(athleteId);
        List<BiomechanicalRecord> bioRecords =
                biomechanicsRepository.findByAthleteId(athleteId);

        String description = String.format(
                "Athlete #%d | %s | Completed Sessions: %d | " +
                "Biomechanical Records: %d | Notes: %s",
                athleteId, reportType,
                sessions.size(), bioRecords.size(), extraNotes);

        ClinicalReport report = new ClinicalReport(
                therapistId, reportType, description);
        clinicalReportRepository.save(report);

        return ResponseEntity.ok(Map.of(
                "message",          "Report generated successfully",
                "reportId",         report.getReportId(),
                "athleteId",        athleteId,
                "sessionsCompiled", sessions.size(),
                "bioRecordsCompiled", bioRecords.size(),
                "description",      description
        ));
    }

    // UC14 — View own reports by therapist
    @GetMapping("/reports/{therapistId}")
    public ResponseEntity<?> getReports(
            @PathVariable int therapistId,
            @RequestHeader("X-User-Id") int userId) {
        ResponseEntity<?> accessError =
                requireOwnedTherapist(userId, therapistId);
        if (accessError != null) return accessError;
        return ResponseEntity.ok(
                clinicalReportRepository
                        .findByTherapistId(therapistId));
    }

    private List<Athlete> getAssignedAthleteList(int therapistId) {
        List<Session> sessions =
                sessionService.getSessionsForTherapist(therapistId);
        return profileService.getAllAthletes()
                .stream()
                .filter(athlete -> sessions.stream()
                        .anyMatch(session -> session.getAthleteId()
                                == athlete.getAthleteId()))
                .toList();
    }

    private ResponseEntity<?> requireOwnedTherapist(
            int userId, int therapistId) {
        var requester = physiotherapistRepository.findByUserId(userId);
        if (requester.isEmpty()
                || requester.get().getTherapistId() != therapistId) {
            return ResponseEntity.status(403).body(Map.of(
                    "error",
                    "You can only access your own therapist records."));
        }
        return null;
    }

    private ResponseEntity<?> requireAssignedAthlete(
            int therapistId, int athleteId) {
        boolean assigned = sessionService.getSessionsForTherapist(therapistId)
                .stream()
                .anyMatch(session -> session.getAthleteId() == athleteId);
        if (!assigned) {
            return ResponseEntity.status(403).body(Map.of(
                    "error",
                    "This athlete is not assigned to this therapist."));
        }
        return null;
    }
}
