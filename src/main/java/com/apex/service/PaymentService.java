package com.apex.service;

import com.apex.domain.*;
import com.apex.repository.interfaces.InvoiceRepository;
import com.apex.repository.interfaces.SessionRepository;
import com.apex.service.observer.NotificationEngine;
import com.apex.service.strategy.BillingStrategy;
import com.apex.service.strategy.InsuranceBilling;
import com.apex.service.strategy.SponsorshipBilling;
import com.apex.service.strategy.StandardBilling;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Strategy Pattern — Context Class
 * Holds a reference to the active BillingStrategy.
 * Delegates fee calculation entirely to the strategy,
 * never performing math itself (SRP + OCP adherence).
 * Handles UC18 (Process Session Billing), UC20 (View Ledger).
 */
@Service
public class PaymentService {

    private BillingStrategy strategy;
    private final InvoiceRepository invoiceRepository;
    private final SessionRepository sessionRepository;
    private final NotificationEngine notificationEngine;

    // Default strategy is Standard Billing
    public PaymentService(InvoiceRepository invoiceRepository,
                          SessionRepository sessionRepository,
                          NotificationEngine notificationEngine) {
        this.invoiceRepository   = invoiceRepository;
        this.sessionRepository   = sessionRepository;
        this.notificationEngine  = notificationEngine;
        this.strategy            = new StandardBilling();
    }

    // Runtime strategy swap — OCP in action
    public void setStrategy(BillingStrategy strategy) {
        this.strategy = strategy;
    }

    // Select strategy from billing type string
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
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException(
                    "Session not found: " + sessionId);
        }

        Session session = sessionOpt.get();
        selectStrategy(billingType);

        // Delegate calculation to active strategy
        double amount = strategy.calculateFees(session);

        Invoice invoice = new Invoice(sessionId, athleteId,
                billingType, amount);
        invoiceRepository.save(invoice);

        // UC21 — Notify athlete of billing
        notificationEngine.notifyObserver(athleteId,
                "Invoice generated: $" + String.format("%.2f", amount) +
                " (" + strategy.getStrategyName() + ")");

        return invoice;
    }

    // UC20 — View Financial Ledger (Admin)
    public List<Invoice> getFullLedger() {
        return invoiceRepository.getLedger();
    }

    // UC20 — View own billing history (Athlete)
    public List<Invoice> getAthleteInvoices(int athleteId) {
        return invoiceRepository.findByAthleteId(athleteId);
    }

    public String getCurrentStrategyName() {
        return strategy.getStrategyName();
    }
}
