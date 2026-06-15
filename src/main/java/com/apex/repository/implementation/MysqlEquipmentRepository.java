package com.apex.repository.implementation;

import com.apex.domain.Equipment;
import com.apex.domain.EquipmentStatus;
import com.apex.repository.interfaces.EquipmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class MysqlEquipmentRepository implements EquipmentRepository {

    private final JdbcTemplate jdbc;

    public MysqlEquipmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Equipment mapRow(ResultSet rs, int rowNum)
            throws SQLException {
        return new Equipment(
                rs.getInt("item_id"),
                rs.getInt("facility_id"),
                rs.getString("item_name"),
                EquipmentStatus.valueOf(rs.getString("item_status")),
                rs.getInt("item_quantity")
        );
    }

    @Override
    public void save(Equipment equipment) {
        String sql = "INSERT INTO equipments " +
                "(facility_id, item_name, item_status, " +
                "item_quantity) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, equipment.getFacilityId());
            ps.setString(2, equipment.getItemName());
            ps.setString(3, equipment.getItemStatus().name());
            ps.setInt(4, equipment.getItemQuantity());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            equipment.setItemId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public Optional<Equipment> findById(int itemId) {
        String sql = "SELECT * FROM equipments WHERE item_id = ?";
        var results = jdbc.query(sql, this::mapRow, itemId);
        return results.isEmpty() ? Optional.empty()
                : Optional.of(results.get(0));
    }

    @Override
    public List<Equipment> findByFacilityId(int facilityId) {
        String sql = "SELECT * FROM equipments " +
                "WHERE facility_id = ?";
        return jdbc.query(sql, this::mapRow, facilityId);
    }

    @Override
    public List<Equipment> findByStatus(EquipmentStatus status) {
        String sql = "SELECT * FROM equipments WHERE item_status = ?";
        return jdbc.query(sql, this::mapRow, status.name());
    }

    @Override
    public void updateStatus(int itemId, EquipmentStatus status) {
        jdbc.update("UPDATE equipments SET item_status = ? " +
                "WHERE item_id = ?", status.name(), itemId);
    }

    @Override
    public void delete(int itemId) {
        jdbc.update("DELETE FROM equipments WHERE item_id = ?",
                itemId);
    }
}
