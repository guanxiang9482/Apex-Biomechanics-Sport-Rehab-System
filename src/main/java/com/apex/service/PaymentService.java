package com.apex.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.apex.domain.Athlete;
import com.apex.domain.BillingType;
import com.apex.domain.Invoice;
import com.apex.domain.Session;
import com.apex.domain.SessionStatus;
import com.apex.repository.interfaces.AthleteRepository;
import com.apex.repository.interfaces.InvoiceRepository;
import com.apex.repository.interfaces.SessionRepository;
import com.apex.service.observer.NotificationEngine;
import com.apex.service.strategy.BillingStrategy;
import com.apex.service.strategy.InsuranceBilling;
import com.apex.service.strategy.SponsorshipBilling;
import com.apex.service.strategy.StandardBilling;

/**
 * Strategy Pattern — Context Class
 * Delegates fee calculation to the active BillingStrategy.
 * Never performs math itself — SRP + OCP adherence.
 *
 * Fix 2: notifyObserver now uses athlete's userId (users.user_id)
 *        not athleteId (athletes.athlete_id). These are different
 *        PKs. Observer is registered with userId on login.
 *
 * Fix 3: Billing now validates:
 *   (a) Session must be COMPLETED before billing.
 *   (b) Provided athleteId must match the session's actual owner.
 *
 * UC18: Process Session Billing
 * UC20: View Financial Ledger
 */
@Service
public class PaymentService {

    private BillingStrategy strategy;
    private final InvoiceRepository invoiceRepository;
    private final SessionRepository sessionRepository;
    private final AthleteRepository athleteRepository;   // Fix 2
    private final NotificationEngine notificationEngine;

    public PaymentService(InvoiceRepository invoiceRepository,
                          SessionRepository sessionRepository,
                          AthleteRepository athleteRepository,
                          NotificationEngine notificationEngine) {
        this.invoiceRepository  = invoiceRepository;
        this.sessionRepository  = sessionRepository;
        this.athleteRepository  = athleteRepository;
        this.notificationEngine = notificationEngine;
        this.strategy           = new StandardBilling();
    }

    // Runtime strategy swap — OCP in action
    public void setStrategy(BillingStrategy strategy) {
        this.strategy = strategy;
    }

    public void selectStrategy(BillingType billingType) {
        this.strategy = switch (billingType) {
            case STANDARD    -> new StandardBilling();
            case INSURANCE   -> new InsuranceBilling();
            case SPONSORSHIP -> new SponsorshipBilling();
        };
    }

    /**
     * UC18 — Process Session Billing
     *
     * @param sessionId   the session to bill (must be COMPLETED)
     * @param athleteId   athletes.athlete_id (PK of athletes table)
     * @param billingType the billing strategy to apply
     */
    public Invoice processSessionBilling(int sessionId,
                                         int athleteId,
                                         BillingType billingType) {
        // --- Validation (Fix 3) ---
        Optional<Session> sessionOpt =
                sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty())
            throw new IllegalArgumentException(
                    "Session not found: #" + sessionId);

        Session session = sessionOpt.get();

        // Fix 3a: Only COMPLETED sessions can be billed
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Session #" + sessionId + " is " +
                    session.getStatus().name() +
                    ". Only COMPLETED sessions can be billed.");
        }

        // Fix 3b: Athlete must own this session
        if (session.getAthleteId() != athleteId) {
            throw new IllegalArgumentException(
                    "Athlete #" + athleteId +
                    " does not own Session #" + sessionId +
                    ". Session belongs to Athlete #" +
                    session.getAthleteId() + ".");
        }

        // --- Strategy pattern: delegate calculation ---
        selectStrategy(billingType);
        double baseAmount   = strategy.calculateFees(session);
        double discountRate = strategy.getDiscountRate();

        Invoice invoice = new Invoice(athleteId, sessionId,
                baseAmount, discountRate, billingType);
        invoiceRepository.save(invoice);

        // --- Fix 2: Resolve athlete's userId for Observer ---
        // Observer is registered with users.user_id on login.
        // athleteId is athletes.athlete_id — a DIFFERENT primary key.
        // We must look up the athlete to get their actual userId.
        Optional<Athlete> athleteOpt =
                athleteRepository.findById(athleteId);
        if (athleteOpt.isPresent()) {
            int athleteUserId = athleteOpt.get().getUserId();
            notificationEngine.notifyObserver(
                    athleteUserId,
                    "Invoice #" + invoice.getInvoiceId() +
                    " generated: RM" +
                    String.format("%.2f", invoice.getFinalAmount()) +
                    " (" + strategy.getStrategyName() + ")");
        }

        return invoice;
    }

    // UC20 — Full ledger (Admin)
    public List<Invoice> getFullLedger() {
        return invoiceRepository.getLedger();
    }

    // UC20 — Athlete's own invoices
    public List<Invoice> getAthleteInvoices(int athleteId) {
        return invoiceRepository.findByAthleteId(athleteId);
    }

    public String getCurrentStrategyName() {
        return strategy.getStrategyName();
    }
}
