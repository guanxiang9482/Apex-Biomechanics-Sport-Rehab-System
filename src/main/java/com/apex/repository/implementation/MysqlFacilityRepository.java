package com.apex.repository.implementation;

import com.apex.domain.Facility;
import com.apex.domain.FacilityStatus;
import com.apex.repository.interfaces.FacilityRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlFacilityRepository implements FacilityRepository {

    private final JdbcTemplate jdbc;

    public MysqlFacilityRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Facility mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        return new Facility(
                rs.getInt("facility_id"),
                rs.getObject("last_used_by_therapist") != null
                        ? rs.getInt("last_used_by_therapist") : null,
                rs.getString("name"),
                rs.getString("type"),
                FacilityStatus.valueOf(rs.getString("status")),
                rs.getString("location")
        );
    }

    @Override
    public List<Facility> findAll() {
        return jdbc.query(
                "SELECT * FROM facilities ORDER BY facility_id",
                this::mapRow);
    }

    @Override
    public Optional<Facility> findById(int facilityId) {
        String sql = "SELECT * FROM facilities WHERE facility_id = ?";
        var results = jdbc.query(sql, this::mapRow, facilityId);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public List<Facility> findByStatus(FacilityStatus status) {
        String sql = "SELECT * FROM facilities WHERE status = ?";
        return jdbc.query(sql, this::mapRow, status.name());
    }

    @Override
    public void updateStatus(int facilityId, FacilityStatus status) {
        jdbc.update("UPDATE facilities SET status = ? " +
                "WHERE facility_id = ?", status.name(), facilityId);
    }

    @Override
    public void updateLastUsedByTherapist(int facilityId,
                                          int therapistId) {
        jdbc.update("UPDATE facilities SET " +
                "last_used_by_therapist = ? " +
                "WHERE facility_id = ?", therapistId, facilityId);
    }
}
