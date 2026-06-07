package com.apex.service.strategy;

import com.apex.domain.Session;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy 3 — Sponsorship Billing
 * Zero-fee billing for high-performance athletes
 * whose costs are absorbed by external sports sponsors.
 * Adding this class required zero modification to
 * PaymentService — OCP demonstrated.
 */
@Component("sponsorshipBilling")
public class SponsorshipBilling implements BillingStrategy {

    @Override
    public double calculateFees(Session session) {
        return 0.0;
    }

    @Override
    public String getStrategyName() {
        return "Sponsorship Billing";
    }
}
