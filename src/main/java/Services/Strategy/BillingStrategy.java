package main.java.Services.Strategy;

import main.java.Repositories.InvoiceRepository;
import main.java.domain.Invoice;
import main.java.domain.Session;

import java.util.List;

public interface BillingStrategy {
    double calculateFees(Session s);
    String getStrategyName();
}

class PaymentService{
    private BillingStrategy strategy;
    private InvoiceRepository invoiceRepo;

    public PaymentService(BillingStrategy strategy) {
        this.strategy = strategy;
    }
    public void setStrategy(BillingStrategy strategy){
        this.strategy = strategy;
    }
    public void executeCalculateFees(Session s){
        double fees = strategy.calculateFees(s);
        // Here we would create an Invoice and save it using invoiceRepo
    }
    public void processPayment(Invoice invoice){
        // Here we would process the payment and update the invoice status
    }
    public List<Invoice> getFinancialLedger(){
        return invoiceRepo.getLedger();
    }
}

class StandardBilling implements BillingStrategy{
    @Override
    public double calculateFees(Session s) {
        return s.getDuration() * 0.5;
    }

    @Override
    public String getStrategyName() {
        return "Standard Billing";
    }
}

class InsuranceBilling implements BillingStrategy{
    @Override
    public double calculateFees(Session s) {
        return s.getDuration() * 0.3;
    }

    @Override
    public String getStrategyName() {
        return "Insurance Billing";
    }
}

class SponsorshipBilling implements BillingStrategy{
    @Override
    public double calculateFees(Session s) {
        return 0;
    }

    @Override
    public String getStrategyName() {
        return "Sponsorship Billing";
    }
}
