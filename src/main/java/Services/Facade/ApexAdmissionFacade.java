package main.java.Services.Facade;


public class ApexAdmissionFacade {
    private AccountService accountService;
    private ProfileService profileService;
    private SessionService sessionService;

    public ApexAdmissionFacade() {
        this.accountService = new AccountService();
        this.profileService = new ProfileService();
        this.sessionService = new SessionService();
    }

    public boolean admitNewAthlete(int athleteId) {
        boolean accountCreated = accountService.createAccount(athleteId);
        if (!accountCreated) {
            return false;
        }

        boolean profileCreated = profileService.createProfile(athleteId);
        if (!profileCreated) {
            accountService.deleteAccount(athleteId); // Rollback account creation
            return false;
        }

        boolean sessionScheduled = sessionService.scheduleInitialSession(athleteId);
        if (!sessionScheduled) {
            profileService.deleteProfile(athleteId); // Rollback profile creation
            accountService.deleteAccount(athleteId); // Rollback account creation
            return false;
        }

        return true; // Admission successful
    }

    public String getAdmissionStatus(int athleteId) {
        // Logic to check the admission status of the athlete
        return "Pending"; // Placeholder return
    }

    public void rollBackAdmission(int athleteId) {
        // Logic to rollback the admission process for the athlete
        sessionService.cancelInitialSession(athleteId);
        profileService.deleteProfile(athleteId);
        accountService.deleteAccount(athleteId);
    }
}
