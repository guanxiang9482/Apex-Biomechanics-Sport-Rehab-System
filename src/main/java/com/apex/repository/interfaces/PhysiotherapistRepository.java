package com.apex.repository.interfaces;

import com.apex.domain.Physiotherapist;

import java.util.List;
import java.util.Optional;

public interface PhysiotherapistRepository {
    Optional<Physiotherapist> findById(int therapistId);
    Optional<Physiotherapist> findByUserId(int userId);
    List<Physiotherapist> findAll();
    void updateProfessionalInfo(int userId, String specialization,
                                String licenseNumber);
}
