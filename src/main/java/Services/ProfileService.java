package main.java.Services;

import main.java.Repositories.AthleteRepository;
import main.java.domain.Athlete;

public class ProfileService {
    private AthleteRepository athleteRepo;

    public ProfileService(AthleteRepository athleteRepo) {
        this.athleteRepo = athleteRepo;
    }
    public Athlete createAthleteProfile(int athleteId) {
        // Logic to create athlete profile to the repository
        return null;
    }
    public void updateProfile(){}
    public Athlete getProfile(){return null;}
    public void initMedicalRecord(){}
}
