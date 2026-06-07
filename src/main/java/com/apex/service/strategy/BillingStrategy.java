package com.apex.service.strategy;

import com.apex.domain.Session;

/**
 * Strategy Pattern — Algorithm Interface
 * Defines the contract for all billing algorithms.
 * PaymentService depends only on this abstraction,
 * never on concrete billing classes (DIP + OCP adherence).
 */
public interface BillingStrategy {
    double calculateFees(Session session);
    String getStrategyName();
}
