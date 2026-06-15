package com.apex.repository.interfaces;

import com.apex.domain.Facility;
import com.apex.domain.FacilityStatus;
import java.util.List;
import java.util.Optional;

public interface FacilityRepository {
    List<Facility> findAll();
    Optional<Facility> findById(int facilityId);
    List<Facility> findByStatus(FacilityStatus status);
    void updateStatus(int facilityId, FacilityStatus status);
    void updateLastUsedByTherapist(int facilityId, int therapistId);
}
