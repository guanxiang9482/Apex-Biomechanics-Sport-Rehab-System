package com.apex.service.facade;

import com.apex.domain.Athlete;

/**
 * Facade Pattern — Interface
 * Defines the simplified contract for the
 * complex athlete admission workflow (UC15).
 */
public interface AdmissionFacade {
    Athlete admitNewAthlete(String username, String password,
                            String email, String fullName,
                            int therapistId, int facilityId);
    void rollBackAdmission(int userId);
    String getAdmissionStatus(int athleteId);
}
