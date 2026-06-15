package com.apex.repository.interfaces;

import java.util.List;
import java.util.Optional;

import com.apex.domain.MedicalRecord;

// ISP: Only clinical record and report operations
public interface MedicalRecordRepository {
    void save(MedicalRecord record);
    Optional<MedicalRecord> findById(int recordId);
    List<MedicalRecord> findByAthleteId(int athleteId);
    List<MedicalRecord> findByTherapistId(int therapistId);
    void update(MedicalRecord record);
    void delete(int recordId);
}
