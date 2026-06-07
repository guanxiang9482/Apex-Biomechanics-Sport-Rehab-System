package com.apex.repository.interfaces;

import com.apex.domain.BiomechanicalRecord;
import java.util.List;
import java.util.Optional;

// ISP: Only biomechanical data operations
public interface BiomechanicsRepository {
    void save(BiomechanicalRecord record);
    Optional<BiomechanicalRecord> findById(int recordId);
    List<BiomechanicalRecord> findBySessionId(int sessionId);
    List<BiomechanicalRecord> findByAthleteId(int athleteId);
    void update(BiomechanicalRecord record);
    void delete(int recordId);
}
