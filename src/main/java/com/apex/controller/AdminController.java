package com.apex.controller;

import com.apex.domain.*;
import com.apex.repository.interfaces.ClinicalReportRepository;
import com.apex.repository.interfaces.FacilityRepository;
import com.apex.service.AccountService;
import com.apex.service.PaymentService;
import com.apex.service.ProfileService;
import com.apex.service.facade.AdmissionFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UC15, UC16, UC17, UC18, UC19, UC20
 * All administrator operations.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdmissionFacade admissionFacade;
    private final ProfileService profileService;
    private final PaymentService paymentService;
    private final AccountService accountService;
    private final FacilityRepository facilityRepository;
    private final ClinicalReportRepository clinicalReportRepository;

    public AdminController(
            AdmissionFacade admissionFacade,
            ProfileService profileService,
            PaymentService paymentService,
            AccountService accountService,
            FacilityRepository facilityRepository,
            ClinicalReportRepository clinicalReportRepository) {
        this.admissionFacade          = admissionFacade;
        this.profileService           = profileService;
        this.paymentService           = paymentService;
        this.accountService           = accountService;
        this.facilityRepository       = facilityRepository;
        this.clinicalReportRepository = clinicalReportRepository;
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

    // UC17 — View facilities
    @GetMapping("/facilities")
    public ResponseEntity<List<Facility>> getFacilities() {
        return ResponseEntity.ok(
                facilityRepository.findAll());
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
                    "message",     "Billing processed",
                    "invoiceId",   invoice.getInvoiceId(),
                    "baseAmount",  invoice.getBaseAmount(),
                    "discount",    invoice.getDiscountRate(),
                    "finalAmount", invoice.getFinalAmount(),
                    "strategy",    paymentService
                            .getCurrentStrategyName()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
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

    // UC19 — Deactivate staff
    @PutMapping("/staff/{userId}/deactivate")
    public ResponseEntity<?> deactivateStaff(
            @PathVariable int userId) {
        accountService.deactivateAccount(userId);
        return ResponseEntity.ok(Map.of(
                "message",
                "Account deactivated successfully"));
    }

    // UC20 — Full financial ledger
    @GetMapping("/ledger")
    public ResponseEntity<List<Invoice>> getFullLedger() {
        return ResponseEntity.ok(
                paymentService.getFullLedger());
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
                "message", "Report approved"));
    }
}
