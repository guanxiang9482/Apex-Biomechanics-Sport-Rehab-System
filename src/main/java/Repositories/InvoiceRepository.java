package main.java.Repositories;

import java.util.List;
import main.java.domain.Invoice;

public interface InvoiceRepository {
    List<Invoice> findbyAthlete(int athleteId);
    List<Invoice> findbySession(int sessionId);
    void save(Invoice invoice);
    void delete(Invoice invoice);
    void update(Invoice invoice);
    List<Invoice> getLedger();
}
