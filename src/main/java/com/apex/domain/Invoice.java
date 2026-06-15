package com.apex.domain;

import java.time.LocalDateTime;


/* Strategy pattern alignment:
 * - StandardBilling:    discountRate = 0.0
 * - InsuranceBilling:   discountRate = 0.40 (40% off)
 * - SponsorshipBilling: discountRate = 1.0  (100% off)*/
public class Invoice {

     private int invoiceId;
    private int athleteId;
    private int sessionId;
    private double baseAmount;
    private double discountRate;
    private double finalAmount;
    private BillingType billingType;
    private PaymentMethod paymentMethod;
    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;
    private InvoiceStatus status;

    // Constructor for new invoice
    public Invoice(int athleteId, int sessionId,
                   double baseAmount, double discountRate,
                   BillingType billingType) {
        this.athleteId     = athleteId;
        this.sessionId     = sessionId;
        this.baseAmount    = baseAmount;
        this.discountRate  = discountRate;
        this.finalAmount   = baseAmount * (1.0 - discountRate);
        this.billingType   = billingType;
        this.paymentMethod = PaymentMethod.PENDING;
        this.issuedAt      = LocalDateTime.now();
        this.status        = InvoiceStatus.PENDING;
    }

    // Constructor for loading from database
    public Invoice(int invoiceId, int athleteId, int sessionId,
                   double baseAmount, double discountRate,
                   double finalAmount, BillingType billingType,
                   PaymentMethod paymentMethod,
                   LocalDateTime issuedAt, LocalDateTime paidAt,
                   InvoiceStatus status) {
        this.invoiceId     = invoiceId;
        this.athleteId     = athleteId;
        this.sessionId     = sessionId;
        this.baseAmount    = baseAmount;
        this.discountRate  = discountRate;
        this.finalAmount   = finalAmount;
        this.billingType   = billingType;
        this.paymentMethod = paymentMethod;
        this.issuedAt      = issuedAt;
        this.paidAt        = paidAt;
        this.status        = status;
    }

    public int getInvoiceId()           { return invoiceId; }
    public int getAthleteId()           { return athleteId; }
    public int getSessionId()           { return sessionId; }
    public double getBaseAmount()       { return baseAmount; }
    public double getDiscountRate()     { return discountRate; }
    public double getFinalAmount()      { return finalAmount; }
    public BillingType getBillingType() { return billingType; }
    public PaymentMethod getPaymentMethod(){ return paymentMethod; }
    public LocalDateTime getIssuedAt()  { return issuedAt; }
    public LocalDateTime getPaidAt()    { return paidAt; }
    public InvoiceStatus getStatus()    { return status; }

    public void setInvoiceId(int id)    { this.invoiceId = id; }
    public void setBaseAmount(double amount) {
        this.baseAmount  = amount;
        this.finalAmount = baseAmount * (1.0 - discountRate);
    }
    public void setBillingType(BillingType type) { this.billingType = type; }
    public void setPaymentMethod(PaymentMethod m){ this.paymentMethod = m; }
    public void setPaidAt(LocalDateTime t){ this.paidAt = t; }
    public void setStatus(InvoiceStatus s){ this.status = s; }
}
