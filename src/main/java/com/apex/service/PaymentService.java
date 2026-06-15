package com.apex.service;

import com.apex.domain.*;
import com.apex.repository.interfaces.InvoiceRepository;
import com.apex.repository.interfaces.SessionRepository;
import com.apex.service.observer.NotificationEngine;
import com.apex.service.strategy.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Strategy Pattern — Context Class
 * Delegates fee calculation to the active BillingStrategy.
 * Never performs math itself — SRP + OCP adherence.
 *
 * Strategy alignment with Invoice fields:
 * Standard:    baseRate * duration, discountRate = 0.0
 * Insurance:   baseRate * duration, discountRate = 0.40
 * Sponsorship: baseRate * duration, discountRate = 1.0
 *
 * UC18: Process Session Billing
 * UC20: View Financial Ledger
 */
@Service
public class PaymentService {

    private BillingStrategy strategy;
    private final InvoiceRepository invoiceRepository;
    private final SessionRepository sessionRepository;
    private final NotificationEngine notificationEngine;

    public PaymentService(InvoiceRepository invoiceRepository,
                          SessionRepository sessionRepository,
                          NotificationEngine notificationEngine) {
        this.invoiceRepository  = invoiceRepository;
        this.sessionRepository  = sessionRepository;
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

    // UC18 — Process Session Billing
    public Invoice processSessionBilling(int sessionId,
                                         int athleteId,
                                         BillingType billingType) {
        Optional<Session> sessionOpt =
                sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty())
            throw new IllegalArgumentException(
                    "Session not found: " + sessionId);

        Session session = sessionOpt.get();
        selectStrategy(billingType);

        // Delegate to strategy — context never calculates
        double baseAmount   = strategy.calculateFees(session);
        double discountRate = strategy.getDiscountRate();

        Invoice invoice = new Invoice(athleteId, sessionId,
                baseAmount, discountRate, billingType);
        invoiceRepository.save(invoice);

        // UC21 — Notify athlete of invoice
        notificationEngine.notifyObserver(athleteId,
                "Invoice generated: RM" +
                String.format("%.2f", invoice.getFinalAmount()) +
                " (" + strategy.getStrategyName() + ")");

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
