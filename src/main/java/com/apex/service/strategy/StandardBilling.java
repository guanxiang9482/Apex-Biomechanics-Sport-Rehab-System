package com.apex.service.strategy;

import com.apex.domain.Session;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy 1 — Standard Billing
 * Full rate, no discount.
 * Rate: RM 2.00 per minute
 */
@Component("standardBilling")
public class StandardBilling implements BillingStrategy {

    private static final double RATE_PER_MINUTE = 2.00;

    @Override
    public double calculateFees(Session session) {
        return session.getDurationMins() * RATE_PER_MINUTE;
    }

    @Override
    public double getDiscountRate() { return 0.0; }

    @Override
    public String getStrategyName() { return "Standard Billing"; }
}
