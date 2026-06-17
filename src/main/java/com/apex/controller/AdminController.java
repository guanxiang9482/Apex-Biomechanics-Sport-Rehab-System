package com.apex.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apex.domain.Athlete;
import com.apex.domain.BillingType;
import com.apex.domain.Equipment;
import com.apex.domain.EquipmentStatus;
import com.apex.domain.Facility;
import com.apex.domain.FacilityStatus;
import com.apex.domain.Invoice;
import com.apex.domain.InvoiceStatus;
import com.apex.domain.Physiotherapist;
import com.apex.domain.ReportStatus;
import com.apex.domain.Role;
import com.apex.domain.Session;
import com.apex.domain.SessionStatus;
import com.apex.domain.User;
import com.apex.repository.interfaces.ClinicalReportRepository;
import com.apex.repository.interfaces.EquipmentRepository;
import com.apex.repository.interfaces.FacilityRepository;
import com.apex.repository.interfaces.PhysiotherapistRepository;
import com.apex.repository.interfaces.UserRepository;
import com.apex.service.AccountService;
import com.apex.service.PaymentService;
import com.apex.service.ProfileService;
import com.apex.service.SessionService;
import com.apex.service.facade.AdmissionFacade;

/**
 * UC15, UC16, UC17, UC18, UC19, UC20
 * All administrator operations.
 *
 * Fix 5 — UC17: Added update facility status + view/update
 *   equipment status endpoints.
 * Fix 5 — UC19: Added view all staff + delete staff endpoints.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdmissionFacade admissionFacade;
    private final ProfileService profileService;
    private final SessionService sessionService;
    private final PaymentService paymentService;
    private final AccountService accountService;
    private final FacilityRepository facilityRepository;
    private final EquipmentRepository equipmentRepository;
    private final ClinicalReportRepository clinicalReportRepository;
    private final UserRepository userRepository;
    private final PhysiotherapistRepository physiotherapistRepository;
    private final JdbcTemplate jdbc;

    public AdminController(
            AdmissionFacade admissionFacade,
            ProfileService profileService,
            SessionService sessionService,
            PaymentService paymentService,
            AccountService accountService,
            FacilityRepository facilityRepository,
            EquipmentRepository equipmentRepository,
            ClinicalReportRepository clinicalReportRepository,
            UserRepository userRepository,
            PhysiotherapistRepository physiotherapistRepository,
            JdbcTemplate jdbc) {
        this.admissionFacade          = admissionFacade;
        this.profileService           = profileService;
        this.sessionService           = sessionService;
        this.paymentService           = paymentService;
        this.accountService           = accountService;
        this.facilityRepository       = facilityRepository;
        this.equipmentRepository      = equipmentRepository;
        this.clinicalReportRepository = clinicalReportRepository;
        this.userRepository           = userRepository;
        this.physiotherapistRepository = physiotherapistRepository;
        this.jdbc                     = jdbc;
    }

    // UC15 — Admit New Athlete (Facade Pattern showcase)
    @PostMapping("/athletes/admit")
    public ResponseEntity<?> admitNewAthlete(
            @RequestBody Map<String, Object> body) {
        try {
            Athlete athlete = admissionFacade.admitNewAthlete(
                    (String) body.get("username"),
                    (String) body.get("password"),
                    (String) body.get("email"),
                    (String) body.get("fullName"),
                    (String) body.getOrDefault("contact", ""),
                    (Integer) body.get("therapistId"),
                    (Integer) body.get("facilityId"));

            return ResponseEntity.ok(Map.of(
                    "message",   "Athlete admitted successfully",
                    "athleteId", athlete.getAthleteId(),
                    "userId",    athlete.getUserId(),
                    "fullname",  athlete.getFullname(),
                    "status",    admissionFacade
                            .getAdmissionStatus(
                                    athlete.getAthleteId())
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC16 — Analytics dashboard
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics() {
        List<Athlete> athletes = profileService.getAllAthletes();
        List<Invoice> ledger   = paymentService.getFullLedger();
        List<Session> sessions = sessionService.getAllSessions();
        List<Facility> facilities = facilityRepository.findAll();

        double totalRevenue = ledger.stream()
                .mapToDouble(Invoice::getFinalAmount)
                .sum();

        long pendingInvoices = ledger.stream()
                .filter(i -> i.getStatus()
                        == InvoiceStatus.PENDING)
                .count();

        Map<Integer, Long> facilityUsage = sessions.stream()
                .filter(session -> session.getFacilityId() > 0)
                .collect(Collectors.groupingBy(
                        Session::getFacilityId, Collectors.counting()));

        Map<String, Object> analytics = new java.util.HashMap<>();
        analytics.put("totalAthletes", athletes.size());
        analytics.put("totalInvoices", ledger.size());
        analytics.put("totalRevenue", totalRevenue);
        analytics.put("pendingInvoices", pendingInvoices);
        analytics.put("totalSessions", sessions.size());
        analytics.put("completedSessions", sessions.stream()
                .filter(session -> session.getStatus()
                        == SessionStatus.COMPLETED).count());
        analytics.put("cancelledSessions", sessions.stream()
                .filter(session -> session.getStatus()
                        == SessionStatus.CANCELLED).count());
        analytics.put("scheduledSessions", sessions.stream()
                .filter(session -> session.getStatus()
                        == SessionStatus.SCHEDULED).count());
        analytics.put("availableFacilities", facilities.stream()
                .filter(facility -> facility.getStatus()
                        == FacilityStatus.AVAILABLE).count());
        analytics.put("maintenanceFacilities", facilities.stream()
                .filter(facility -> facility.getStatus()
                        == FacilityStatus.MAINTENANCE).count());
        analytics.put("facilityUsage", facilityUsage);
        return ResponseEntity.ok(analytics);
    }

    // UC16 — All athletes
    @GetMapping("/athletes")
    public ResponseEntity<List<Athlete>> getAllAthletes() {
        return ResponseEntity.ok(
                profileService.getAllAthletes());
    }

    @GetMapping("/sessions/completed")
    public ResponseEntity<List<Session>> getCompletedSessions() {
        return ResponseEntity.ok(sessionService.getCompletedSessions());
    }

    // ─── UC17: Facility & Equipment Management (Fix 5) ───────────

    // UC17 — View all facilities
    @GetMapping("/facilities")
    public ResponseEntity<List<Facility>> getFacilities() {
        return ResponseEntity.ok(
                facilityRepository.findAll());
    }

    // UC17 — Update facility status (AVAILABLE / MAINTENANCE / RESERVED)
    @PutMapping("/facilities/{facilityId}/status")
    public ResponseEntity<?> updateFacilityStatus(
            @PathVariable int facilityId,
            @RequestBody Map<String, String> body) {
        try {
            FacilityStatus status =
                    FacilityStatus.valueOf(body.get("status"));
            facilityRepository.updateStatus(facilityId, status);
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Facility #" + facilityId +
                    " status updated to " + status.name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Invalid status. Use: AVAILABLE, " +
                            "MAINTENANCE, RESERVED"));
        }
    }

    // UC17 — View equipment by facility
    @GetMapping("/facilities/{facilityId}/equipment")
    public ResponseEntity<List<Equipment>> getEquipmentByFacility(
            @PathVariable int facilityId) {
        return ResponseEntity.ok(
                equipmentRepository.findByFacilityId(facilityId));
    }

    // UC17 — Update equipment status (AVAILABLE / IN_USE / MAINTENANCE)
    @PutMapping("/equipment/{itemId}/status")
    public ResponseEntity<?> updateEquipmentStatus(
            @PathVariable int itemId,
            @RequestBody Map<String, String> body) {
        try {
            EquipmentStatus status =
                    EquipmentStatus.valueOf(body.get("status"));
            equipmentRepository.updateStatus(itemId, status);
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Equipment #" + itemId +
                    " status updated to " + status.name()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Invalid status. Use: AVAILABLE, " +
                            "IN_USE, MAINTENANCE"));
        }
    }

    // ─── UC18: Process Session Billing ───────────────────────────

    @GetMapping("/therapists")
    public ResponseEntity<List<Physiotherapist>> getTherapists() {
        return ResponseEntity.ok(
                physiotherapistRepository.findAll());
    }

    // UC18 — Process billing (Strategy Pattern showcase)
    @PostMapping("/billing/process")
    public ResponseEntity<?> processBilling(
            @RequestBody Map<String, Object> body) {
        try {
            int sessionId  = (Integer) body.get("sessionId");
            int athleteId  = (Integer) body.get("athleteId");
            BillingType bt = BillingType.valueOf(
                    (String) body.get("billingType"));

            Invoice invoice = paymentService
                    .processSessionBilling(
                            sessionId, athleteId, bt);

            return ResponseEntity.ok(Map.of(
                    "message",     "Billing processed successfully",
                    "invoiceId",   invoice.getInvoiceId(),
                    "baseAmount",  invoice.getBaseAmount(),
                    "discount",    invoice.getDiscountRate(),
                    "finalAmount", invoice.getFinalAmount(),   // Fix 7 alignment
                    "strategy",    paymentService
                            .getCurrentStrategyName()
            ));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC20 — Full financial ledger
    @GetMapping("/ledger")
    public ResponseEntity<List<Invoice>> getFullLedger() {
        return ResponseEntity.ok(
                paymentService.getFullLedger());
    }

    // ─── UC19: Staff Profile Management (Fix 5) ──────────────────

    // UC19 — View all staff (therapists + admins)
    @GetMapping("/staff")
    public ResponseEntity<?> getAllStaff() {
        List<Map<String, Object>> staff = userRepository.findAllStaff()
                .stream()
                .map(user -> {
                    Map<String, Object> row = new java.util.HashMap<>();
                    row.put("userId", user.getUserId());
                    row.put("username", user.getUsername());
                    row.put("email", user.getEmail());
                    row.put("role", user.getRole().name());
                    row.put("fullname", user.getFullname());
                    row.put("contact", user.getContact());
                    row.put("active", user.isActive());
                    if (user.getRole() == Role.THERAPIST) {
                        physiotherapistRepository.findByUserId(user.getUserId())
                                .ifPresent(therapist -> {
                                    row.put("specialization",
                                            therapist.getSpecialization());
                                    row.put("licenseNumber",
                                            therapist.getLicenseNumber());
                                });
                    }
                    return row;
                })
                .toList();
        return ResponseEntity.ok(staff);
    }

    // UC19 — Add staff (Therapist or Admin)
    @PostMapping("/staff/add")
    public ResponseEntity<?> addStaff(
            @RequestBody Map<String, String> body) {
        try {
            Role role = Role.valueOf(body.get("role"));
            if (role == Role.ATHLETE) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error",
                                "Staff accounts must be ADMIN or THERAPIST."));
            }
            User staff = accountService.createAccount(
                    body.get("username"),
                    body.get("password"),
                    body.get("email"),
                    body.get("fullName"),
                    body.getOrDefault("contact", ""),
                    role);

            return ResponseEntity.ok(Map.of(
                    "message", "Staff account created",
                    "userId",  staff.getUserId(),
                    "role",    staff.getRole().name()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC19 — Deactivate staff (soft delete)
    // UC19 - Update staff profile details
    @PutMapping("/staff/{userId}")
    public ResponseEntity<?> updateStaff(
            @PathVariable int userId,
            @RequestBody Map<String, String> body,
            @RequestHeader("X-User-Id") int requestUserId) {
        try {
            ResponseEntity<?> validation =
                    validateStaffManagementTarget(userId, requestUserId);
            if (validation != null) return validation;

            User staff = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Staff account not found."));

            String email = body.getOrDefault("email", staff.getEmail());
            String fullname = body.getOrDefault("fullName",
                    staff.getFullname());
            String contact = body.getOrDefault("contact",
                    staff.getContact() == null ? "" : staff.getContact());
            userRepository.updateStaffInfo(userId, email, fullname, contact);

            if (staff.getRole() == Role.THERAPIST) {
                var therapist = physiotherapistRepository.findByUserId(userId);
                if (therapist.isPresent()) {
                    String specialization = body.getOrDefault(
                            "specialization",
                            therapist.get().getSpecialization());
                    String licenseNumber = body.getOrDefault(
                            "licenseNumber",
                            therapist.get().getLicenseNumber());
                    physiotherapistRepository.updateProfessionalInfo(
                            userId, specialization, licenseNumber);
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Staff profile updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/staff/{userId}/deactivate")
    public ResponseEntity<?> deactivateStaff(
            @PathVariable int userId,
            @RequestHeader("X-User-Id") int requestUserId) {
        try {
            ResponseEntity<?> validation =
                    validateStaffManagementTarget(userId, requestUserId);
            if (validation != null) return validation;
            accountService.deactivateAccount(userId);
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Account #" + userId + " deactivated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC19 — Hard delete staff account
    @DeleteMapping("/staff/{userId}")
    public ResponseEntity<?> deleteStaff(
            @PathVariable int userId,
            @RequestHeader("X-User-Id") int requestUserId) {
        try {
            ResponseEntity<?> validation =
                    validateStaffManagementTarget(userId, requestUserId);
            if (validation != null) return validation;
            accountService.deleteAccount(userId);
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Staff account #" + userId + " permanently deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UC14 — Approve clinical report
    @GetMapping("/reports/submitted")
    public ResponseEntity<?> getSubmittedReports() {
        return ResponseEntity.ok(
                clinicalReportRepository.findByStatus(
                        ReportStatus.SUBMITTED));
    }

    @PutMapping("/reports/{reportId}/approve")
    public ResponseEntity<?> approveReport(
            @PathVariable int reportId,
            @RequestHeader("X-User-Id") int requestUserId) {
        int adminId = getAdminIdByUserId(requestUserId);
        return clinicalReportRepository.findById(reportId)
                .map(report -> {
                    report.approve(adminId);
                    clinicalReportRepository.update(report);
                    return ResponseEntity.ok(Map.of(
                            "message",
                            "Report #" + reportId + " approved"));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private int getAdminIdByUserId(int userId) {
        List<Integer> ids = jdbc.query(
                "SELECT admin_id FROM administrators WHERE user_id = ?",
                (rs, rowNum) -> rs.getInt("admin_id"),
                userId);
        if (ids.isEmpty()) {
            throw new IllegalArgumentException(
                    "Admin profile not found for this user.");
        }
        return ids.get(0);
    }

    private ResponseEntity<?> validateStaffManagementTarget(
            int targetUserId, int requestUserId) {
        if (targetUserId == requestUserId) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error",
                    "You cannot deactivate or delete your own admin account."));
        }

        return userRepository.findById(targetUserId)
                .map(target -> {
                    if (target.getRole() == Role.ATHLETE) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "error",
                                "This action is only for staff accounts."));
                    }
                    return null;
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
