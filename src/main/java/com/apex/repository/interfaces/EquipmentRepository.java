package com.apex.repository.interfaces;

import com.apex.domain.Equipment;
import com.apex.domain.EquipmentStatus;
import java.util.List;
import java.util.Optional;

public interface EquipmentRepository {
    void save(Equipment equipment);
    Optional<Equipment> findById(int itemId);
    List<Equipment> findByFacilityId(int facilityId);
    List<Equipment> findByStatus(EquipmentStatus status);
    void updateStatus(int itemId, EquipmentStatus status);
    void delete(int itemId);
}
