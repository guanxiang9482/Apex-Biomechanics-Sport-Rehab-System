package com.apex.repository.interfaces;

import java.util.List;
import java.util.Optional;

import com.apex.domain.Athlete;

// ISP: Only athlete profile operations
public interface AthleteRepository {
    void save(Athlete athlete);
    Optional<Athlete> findById(int athleteId);
    Optional<Athlete> findByUserId(int userId);
    List<Athlete> findAll();
    void updateProfile(Athlete athlete);
    void delete(int athleteId);
}
