package com.apex.repository.interfaces;

import java.util.List;
import java.util.Optional;

import com.apex.domain.Invoice;
import com.apex.domain.InvoiceStatus;

// ISP: Only billing and financial operations
public interface InvoiceRepository {
    void save(Invoice invoice);
    Optional<Invoice> findById(int invoiceId);
    List<Invoice> findByAthleteId(int athleteId);
    List<Invoice> findBySessionId(int sessionId);
    List<Invoice> getLedger();
    void updateStatus(int invoiceId, InvoiceStatus status);
    void delete(int invoiceId);
}
