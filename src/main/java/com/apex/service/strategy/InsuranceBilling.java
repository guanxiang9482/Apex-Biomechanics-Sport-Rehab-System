package com.apex.service.strategy;

import com.apex.domain.Session;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy 2 — Insurance Billing
 * Applies a discounted coverage rate for athletes
 * with third-party medical insurance.
 * Rate: $0.30 per minute
 */
@Component("insuranceBilling")
public class InsuranceBilling implements BillingStrategy {

    private static final double INSURANCE_RATE_PER_MINUTE = 0.30;

    @Override
    public double calculateFees(Session session) {
        return session.getDurationMins() * INSURANCE_RATE_PER_MINUTE;
    }

    @Override
    public String getStrategyName() {
        return "Insurance Billing";
    }
}
