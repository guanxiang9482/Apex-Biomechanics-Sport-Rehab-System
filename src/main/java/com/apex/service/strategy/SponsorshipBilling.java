package com.apex.service.strategy;

import com.apex.domain.Session;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy 3 — Sponsorship Billing
 * 100% covered by sponsor — athlete pays nothing.
 * Adding this class required zero changes to
 * PaymentService — OCP demonstrated.
 */
@Component("sponsorshipBilling")
public class SponsorshipBilling implements BillingStrategy {

    private static final double RATE_PER_MINUTE = 2.00;
    private static final double DISCOUNT         = 1.0;

    @Override
    public double calculateFees(Session session) {
        return session.getDurationMins() * RATE_PER_MINUTE;
    }

    @Override
    public double getDiscountRate() { return DISCOUNT; }

    @Override
    public String getStrategyName() {
        return "Sponsorship Billing";
    }
}
