package com.apex.repository.implementation;

import com.apex.domain.*;
import com.apex.repository.interfaces.InvoiceRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlInvoiceRepository implements InvoiceRepository {

    private final JdbcTemplate jdbc;

    public MysqlInvoiceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Invoice mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        return new Invoice(
                rs.getInt("invoice_id"),
                rs.getInt("athlete_id"),
                rs.getInt("session_id"),
                rs.getDouble("base_amount"),
                rs.getDouble("discount_rate"),
                rs.getDouble("final_amount"),
                BillingType.valueOf(rs.getString("billing_type")),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                rs.getTimestamp("issued_at").toLocalDateTime(),
                rs.getTimestamp("paid_at") != null
                        ? rs.getTimestamp("paid_at").toLocalDateTime()
                        : null,
                InvoiceStatus.valueOf(rs.getString("status"))
        );
    }

    @Override
    public void save(Invoice invoice) {
        String sql = "INSERT INTO invoices " +
                "(athlete_id, session_id, base_amount, " +
                "discount_rate, final_amount, billing_type, " +
                "payment_method, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, invoice.getAthleteId());
            ps.setInt(2, invoice.getSessionId());
            ps.setDouble(3, invoice.getBaseAmount());
            ps.setDouble(4, invoice.getDiscountRate());
            ps.setDouble(5, invoice.getFinalAmount());
            ps.setString(6, invoice.getBillingType().name());
            ps.setString(7, invoice.getPaymentMethod().name());
            ps.setString(8, invoice.getStatus().name());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            invoice.setInvoiceId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public Optional<Invoice> findById(int invoiceId) {
        String sql = "SELECT * FROM invoices WHERE invoice_id = ?";
        var results = jdbc.query(sql, this::mapRow, invoiceId);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public List<Invoice> findByAthleteId(int athleteId) {
        String sql = "SELECT * FROM invoices WHERE athlete_id = ? " +
                "ORDER BY issued_at DESC";
        return jdbc.query(sql, this::mapRow, athleteId);
    }

    @Override
    public List<Invoice> findBySessionId(int sessionId) {
        String sql = "SELECT * FROM invoices WHERE session_id = ?";
        return jdbc.query(sql, this::mapRow, sessionId);
    }

    @Override
    public List<Invoice> getLedger() {
        String sql = "SELECT * FROM invoices ORDER BY issued_at DESC";
        return jdbc.query(sql, this::mapRow);
    }

    @Override
    public void updateStatus(int invoiceId, InvoiceStatus status) {
        jdbc.update("UPDATE invoices SET status = ? " +
                "WHERE invoice_id = ?", status.name(), invoiceId);
    }

    @Override
    public void markPaid(int invoiceId, PaymentMethod paymentMethod) {
        jdbc.update("UPDATE invoices SET status = ?, " +
                "payment_method = ?, paid_at = NOW() " +
                "WHERE invoice_id = ?",
                InvoiceStatus.PAID.name(),
                paymentMethod.name(),
                invoiceId);
    }

    @Override
    public void delete(int invoiceId) {
        jdbc.update("DELETE FROM invoices WHERE invoice_id = ?",
                invoiceId);
    }
}
