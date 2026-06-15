package com.apex.service.strategy;

import com.apex.domain.Session;

/**
 * Strategy Pattern — Algorithm Interface
 * Defines the contract for all billing algorithms.
 * PaymentService depends only on this abstraction — DIP.
 * New billing types can be added without modifying
 * PaymentService — OCP.
 */
public interface BillingStrategy {
    double calculateFees(Session session);
    double getDiscountRate();
    String getStrategyName();
}
