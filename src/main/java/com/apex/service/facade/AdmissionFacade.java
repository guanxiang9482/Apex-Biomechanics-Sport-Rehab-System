package com.apex.service.facade;

import com.apex.domain.Athlete;

public interface AdmissionFacade {
    Athlete admitNewAthlete(String username, String password,
                            String email, String fullname,
                            String contact, int therapistId,
                            int facilityId);
    void rollBackAdmission(int userId);
    String getAdmissionStatus(int athleteId);
}
