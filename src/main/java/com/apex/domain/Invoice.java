package com.apex.domain;

import java.time.LocalDateTime;

public class Invoice {

    private int invoiceId;
    private int sessionId;
    private int athleteId;
    private BillingType billingType;
    private double amount;
    private InvoiceStatus status;
    private LocalDateTime createdAt;

    // Constructor for new invoice
    public Invoice(int sessionId, int athleteId,
                   BillingType billingType, double amount) {
        this.sessionId   = sessionId;
        this.athleteId   = athleteId;
        this.billingType = billingType;
        this.amount      = amount;
        this.status      = InvoiceStatus.PENDING;
        this.createdAt   = LocalDateTime.now();
    }

    // Constructor for loading from database
    public Invoice(int invoiceId, int sessionId, int athleteId,
                   BillingType billingType, double amount,
                   InvoiceStatus status, LocalDateTime createdAt) {
        this.invoiceId   = invoiceId;
        this.sessionId   = sessionId;
        this.athleteId   = athleteId;
        this.billingType = billingType;
        this.amount      = amount;
        this.status      = status;
        this.createdAt   = createdAt;
    }

    // Getters
    public int getInvoiceId()           { return invoiceId; }
    public int getSessionId()           { return sessionId; }
    public int getAthleteId()           { return athleteId; }
    public BillingType getBillingType() { return billingType; }
    public double getAmount()           { return amount; }
    public InvoiceStatus getStatus()    { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setInvoiceId(int invoiceId)      { this.invoiceId = invoiceId; }
    public void setStatus(InvoiceStatus status)  { this.status = status; }
    public void setAmount(double amount)         { this.amount = amount; }
}
