package com.apex.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.apex.domain.Role;
import com.apex.domain.User;
import com.apex.repository.interfaces.ClinicalReportRepository;
import com.apex.repository.interfaces.EquipmentRepository;
import com.apex.repository.interfaces.FacilityRepository;
import com.apex.repository.interfaces.PhysiotherapistRepository;
import com.apex.repository.interfaces.UserRepository;
import com.apex.service.AccountService;
import com.apex.service.PaymentService;
import com.apex.service.ProfileService;
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
    private final PaymentService paymentService;
    private final AccountService accountService;
    private final FacilityRepository facilityRepository;
    private final EquipmentRepository equipmentRepository;
    private final ClinicalReportRepository clinicalReportRepository;
<<<<<<< HEAD
    private final UserRepository userRepository;
=======
    private final PhysiotherapistRepository physiotherapistRepository;
>>>>>>> 98ea01b18eb78eda5cfacd0cc6df91d79caf0d9c

    public AdminController(
            AdmissionFacade admissionFacade,
            ProfileService profileService,
            PaymentService paymentService,
            AccountService accountService,
            FacilityRepository facilityRepository,
<<<<<<< HEAD
            EquipmentRepository equipmentRepository,
            ClinicalReportRepository clinicalReportRepository,
            UserRepository userRepository) {
=======
            ClinicalReportRepository clinicalReportRepository,
            PhysiotherapistRepository physiotherapistRepository) {
>>>>>>> 98ea01b18eb78eda5cfacd0cc6df91d79caf0d9c
        this.admissionFacade          = admissionFacade;
        this.profileService           = profileService;
        this.paymentService           = paymentService;
        this.accountService           = accountService;
        this.facilityRepository       = facilityRepository;
        this.equipmentRepository      = equipmentRepository;
        this.clinicalReportRepository = clinicalReportRepository;
<<<<<<< HEAD
        this.userRepository           = userRepository;
=======
        this.physiotherapistRepository = physiotherapistRepository;
>>>>>>> 98ea01b18eb78eda5cfacd0cc6df91d79caf0d9c
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

        double totalRevenue = ledger.stream()
                .mapToDouble(Invoice::getFinalAmount)
                .sum();

        long pendingInvoices = ledger.stream()
                .filter(i -> i.getStatus()
                        == InvoiceStatus.PENDING)
                .count();

        return ResponseEntity.ok(Map.of(
                "totalAthletes",   athletes.size(),
                "totalInvoices",   ledger.size(),
                "totalRevenue",    totalRevenue,
                "pendingInvoices", pendingInvoices
        ));
    }

    // UC16 — All athletes
    @GetMapping("/athletes")
    public ResponseEntity<List<Athlete>> getAllAthletes() {
        return ResponseEntity.ok(
                profileService.getAllAthletes());
    }

    // ─── UC17: Facility & Equipment Management (Fix 5) ───────────

    // UC17 — View all facilities
    @GetMapping("/facilities")
    public ResponseEntity<List<Facility>> getFacilities() {
        return ResponseEntity.ok(
                facilityRepository.findAll());
    }

<<<<<<< HEAD
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

=======
    @GetMapping("/therapists")
    public ResponseEntity<List<Physiotherapist>> getTherapists() {
        return ResponseEntity.ok(
                physiotherapistRepository.findAll());
    }

    // UC18 — Process billing (Strategy Pattern showcase)
>>>>>>> 98ea01b18eb78eda5cfacd0cc6df91d79caf0d9c
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
        List<User> staff = userRepository.findAllStaff();
        return ResponseEntity.ok(staff);
    }

    // UC19 — Add staff (Therapist or Admin)
    @PostMapping("/staff/add")
    public ResponseEntity<?> addStaff(
            @RequestBody Map<String, String> body) {
        try {
            User staff = accountService.createAccount(
                    body.get("username"),
                    body.get("password"),
                    body.get("email"),
                    body.get("fullName"),
                    body.getOrDefault("contact", ""),
                    Role.valueOf(body.get("role")));

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
    @PutMapping("/staff/{userId}/deactivate")
    public ResponseEntity<?> deactivateStaff(
            @PathVariable int userId) {
        accountService.deactivateAccount(userId);
        return ResponseEntity.ok(Map.of(
                "message",
                "Account #" + userId + " deactivated successfully"));
    }

    // UC19 — Hard delete staff account
    @DeleteMapping("/staff/{userId}")
    public ResponseEntity<?> deleteStaff(
            @PathVariable int userId) {
        accountService.deleteAccount(userId);
        return ResponseEntity.ok(Map.of(
                "message",
                "Staff account #" + userId + " permanently deleted"));
    }

    // UC14 — Approve clinical report
    @PutMapping("/reports/{reportId}/approve")
    public ResponseEntity<?> approveReport(
            @PathVariable int reportId,
            @RequestBody Map<String, Integer> body) {
        int adminId = body.get("adminId");
        clinicalReportRepository.findById(reportId)
                .ifPresent(report -> {
                    report.approve(adminId);
                    clinicalReportRepository.update(report);
                });
        return ResponseEntity.ok(Map.of(
                "message", "Report #" + reportId + " approved"));
    }
}
