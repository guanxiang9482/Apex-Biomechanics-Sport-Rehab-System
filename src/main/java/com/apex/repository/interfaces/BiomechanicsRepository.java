package com.apex.repository.interfaces;

import java.util.List;
import java.util.Optional;

import com.apex.domain.BiomechanicalRecord;

// ISP: Only biomechanical data operations
public interface BiomechanicsRepository {
    void save(BiomechanicalRecord record);
    Optional<BiomechanicalRecord> findById(int recordId);
    List<BiomechanicalRecord> findBySessionId(int sessionId);
    List<BiomechanicalRecord> findByAthleteId(int athleteId);
    List<BiomechanicalRecord> findByTherapistId(int therapistId);
    void update(BiomechanicalRecord record);
    void delete(int recordId);
}
