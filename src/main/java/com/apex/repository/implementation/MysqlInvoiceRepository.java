package com.apex.repository.implementation;

import com.apex.domain.BillingType;
import com.apex.domain.Invoice;
import com.apex.domain.InvoiceStatus;
import com.apex.repository.interfaces.InvoiceRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlInvoiceRepository implements InvoiceRepository {

    private final JdbcTemplate jdbc;

    public MysqlInvoiceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Invoice mapRowToInvoice(ResultSet rs, int rowNum)
            throws SQLException {
        return new Invoice(
                rs.getInt("invoice_id"),
                rs.getInt("session_id"),
                rs.getInt("athlete_id"),
                BillingType.valueOf(rs.getString("billing_type")),
                rs.getDouble("amount"),
                InvoiceStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    @Override
    public void save(Invoice invoice) {
        String sql = "INSERT INTO invoices " +
                     "(session_id, athlete_id, billing_type, " +
                     "amount, status) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, invoice.getSessionId());
            ps.setInt(2, invoice.getAthleteId());
            ps.setString(3, invoice.getBillingType().name());
            ps.setDouble(4, invoice.getAmount());
            ps.setString(5, invoice.getStatus().name());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            invoice.setInvoiceId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public Optional<Invoice> findById(int invoiceId) {
        String sql = "SELECT * FROM invoices WHERE invoice_id = ?";
        var results = jdbc.query(sql, this::mapRowToInvoice, invoiceId);
        return results.isEmpty() ? Optional.empty()
                                 : Optional.of(results.get(0));
    }

    @Override
    public List<Invoice> findByAthleteId(int athleteId) {
        String sql = "SELECT * FROM invoices WHERE athlete_id = ? " +
                     "ORDER BY created_at DESC";
        return jdbc.query(sql, this::mapRowToInvoice, athleteId);
    }

    @Override
    public List<Invoice> findBySessionId(int sessionId) {
        String sql = "SELECT * FROM invoices WHERE session_id = ?";
        return jdbc.query(sql, this::mapRowToInvoice, sessionId);
    }

    @Override
    public List<Invoice> getLedger() {
        String sql = "SELECT * FROM invoices ORDER BY created_at DESC";
        return jdbc.query(sql, this::mapRowToInvoice);
    }

    @Override
    public void updateStatus(int invoiceId, InvoiceStatus status) {
        String sql = "UPDATE invoices SET status = ? " +
                     "WHERE invoice_id = ?";
        jdbc.update(sql, status.name(), invoiceId);
    }

    @Override
    public void delete(int invoiceId) {
        String sql = "DELETE FROM invoices WHERE invoice_id = ?";
        jdbc.update(sql, invoiceId);
    }
}
