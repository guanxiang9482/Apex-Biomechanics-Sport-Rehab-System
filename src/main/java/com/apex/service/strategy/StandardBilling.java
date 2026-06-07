package com.apex.service.strategy;

import com.apex.domain.Session;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy 1 — Standard Billing
 * Calculates fees based on session duration
 * multiplied by the base clinic rate.
 * Rate: $0.50 per minute
 */
@Component("standardBilling")
public class StandardBilling implements BillingStrategy {

    private static final double RATE_PER_MINUTE = 0.50;

    @Override
    public double calculateFees(Session session) {
        return session.getDurationMins() * RATE_PER_MINUTE;
    }

    @Override
    public String getStrategyName() {
        return "Standard Billing";
    }
}
