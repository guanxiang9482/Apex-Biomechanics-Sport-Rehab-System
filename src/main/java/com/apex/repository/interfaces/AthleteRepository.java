package com.apex.repository.interfaces;

import com.apex.domain.Athlete;
import java.util.List;
import java.util.Optional;

// ISP: Only athlete profile operations
public interface AthleteRepository {
    void save(Athlete athlete);
    Optional<Athlete> findById(int athleteId);
    List<Athlete> findAll();
    void updateProfile(Athlete athlete);
    void delete(int athleteId);
}
