package com.apex.service.strategy;

import com.apex.domain.Session;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy 2 — Insurance Billing
 * 40% discount for insured athletes.
 * Rate: RM 2.00 per minute, 40% covered by insurance.
 */
@Component("insuranceBilling")
public class InsuranceBilling implements BillingStrategy {

    private static final double RATE_PER_MINUTE = 2.00;
    private static final double DISCOUNT         = 0.40;

    @Override
    public double calculateFees(Session session) {
        return session.getDurationMins() * RATE_PER_MINUTE;
    }

    @Override
    public double getDiscountRate() { return DISCOUNT; }

    @Override
    public String getStrategyName() { return "Insurance Billing"; }
}
