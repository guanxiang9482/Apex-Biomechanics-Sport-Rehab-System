package com.apex.service.facade;

import com.apex.domain.*;
import com.apex.service.AccountService;
import com.apex.service.ProfileService;
import com.apex.service.SessionService;
import com.apex.service.observer.NotificationEngine;
import org.springframework.stereotype.Service;

/**
 * Facade Pattern — UC15: Admit New Athlete
 * Hides complexity of multi-step admission.
 * Administrator calls ONE method — Facade orchestrates:
 *
 * Step 1: AccountService.createAccount() — credentials
 * Step 2: ProfileService (via AccountService) — clinical profile
 * Step 3: SessionService.scheduleInitialSession() — booking
 * Step 4: NotificationEngine.notifyObserver() — Observer fires
 *
 * Atomic rollback on any step failure.
 */
@Service
public class ApexAdmissionFacade implements AdmissionFacade {

    private final AccountService accountService;
    private final ProfileService profileService;
    private final SessionService sessionService;
    private final NotificationEngine notificationEngine;

    public ApexAdmissionFacade(AccountService accountService,
                                ProfileService profileService,
                                SessionService sessionService,
                                NotificationEngine notificationEngine) {
        this.accountService     = accountService;
        this.profileService     = profileService;
        this.sessionService     = sessionService;
        this.notificationEngine = notificationEngine;
    }

    @Override
    public Athlete admitNewAthlete(String username, String password,
                                   String email, String fullname,
                                   String contact, int therapistId,
                                   int facilityId) {
        User createdUser = null;
        try {
            // Step 1 + 2 — Account + Profile (atomic in AccountService)
            createdUser = accountService.createAccount(
                    username, password, email,
                    fullname, contact, Role.ATHLETE);

            int userId = createdUser.getUserId();

            // Get athleteId from the athletes table
            Athlete athlete = profileService
                    .getProfileByUserId(userId)
                    .orElseThrow(() -> new RuntimeException(
                            "Athlete profile not found after creation"));

            // Step 3 — Schedule initial evaluation session
            sessionService.scheduleInitialSession(
                    athlete.getAthleteId(), therapistId, facilityId);

            // Step 4 - Observer fires for the admitted athlete.
            notificationEngine.notifyObserver(
                    athlete.getUserId(),
                    "Admission completed for " + fullname +
                    ". Initial evaluation session scheduled.");

            return athlete;

        } catch (Exception e) {
            // Atomic rollback
            rollBackAdmission(
                    createdUser != null
                            ? createdUser.getUserId() : -1);
            throw new RuntimeException(
                    "Admission failed, rolled back: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public void rollBackAdmission(int userId) {
        if (userId <= 0) return;
        try {
            profileService.getProfileByUserId(userId)
                    .ifPresent(a -> {
                        sessionService.cancelInitialSession(
                                a.getAthleteId());
                        profileService.deleteProfile(
                                a.getAthleteId());
                    });
            accountService.deleteAccount(userId);
        } catch (Exception e) {
            System.err.println("Rollback warning: "
                    + e.getMessage());
        }
    }

    @Override
    public String getAdmissionStatus(int athleteId) {
        return profileService.getProfile(athleteId)
                .map(a -> "ADMITTED - " + a.getFullname())
                .orElse("NOT_FOUND");
    }
}
