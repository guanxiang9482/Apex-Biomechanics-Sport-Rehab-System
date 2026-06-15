package com.apex.service;

import com.apex.domain.Athlete;
import com.apex.repository.interfaces.AthleteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SRP: Responsible strictly for athlete
 * clinical profile management.
 * UC6: Manage Personal Profile
 */
@Service
public class ProfileService {

    private final AthleteRepository athleteRepository;

    public ProfileService(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    // Called by Facade during UC15 admission
    public void createAthleteProfile(Athlete athlete) {
        athleteRepository.save(athlete);
    }

    // UC6 — Update profile
    public void updateProfile(Athlete athlete) {
        athleteRepository.updateProfile(athlete);
    }

    // Get by athleteId (athletes.athlete_id)
    public Optional<Athlete> getProfile(int athleteId) {
        return athleteRepository.findById(athleteId);
    }

    // Get by userId (users.user_id) — used after login
    public Optional<Athlete> getProfileByUserId(int userId) {
        return athleteRepository.findByUserId(userId);
    }

    public List<Athlete> getAllAthletes() {
        return athleteRepository.findAll();
    }

    public void deleteProfile(int athleteId) {
        athleteRepository.delete(athleteId);
    }
}
