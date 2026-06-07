package com.apex.service;

import com.apex.domain.Athlete;
import com.apex.repository.interfaces.AthleteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SRP: Responsible strictly for athlete clinical
 * profile and physical metrics management.
 * Handles UC6 (Manage Personal Profile).
 */
@Service
public class ProfileService {

    private final AthleteRepository athleteRepository;

    public ProfileService(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    // Called by Facade during admission (UC15)
    public void createAthleteProfile(Athlete athlete) {
        athleteRepository.save(athlete);
    }

    // UC6 — Manage Personal Profile
    public void updateProfile(Athlete athlete) {
        athleteRepository.updateProfile(athlete);
    }

    public Optional<Athlete> getProfile(int athleteId) {
        return athleteRepository.findById(athleteId);
    }

    public List<Athlete> getAllAthletes() {
        return athleteRepository.findAll();
    }

    public void deleteProfile(int athleteId) {
        athleteRepository.delete(athleteId);
    }
}
