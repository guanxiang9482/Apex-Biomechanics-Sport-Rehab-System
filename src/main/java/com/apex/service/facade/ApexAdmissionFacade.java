package com.apex.service.facade;

import com.apex.domain.Athlete;
import com.apex.domain.Role;
import com.apex.domain.User;
import com.apex.service.AccountService;
import com.apex.service.ProfileService;
import com.apex.service.SessionService;
import com.apex.service.observer.NotificationEngine;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Facade Pattern — UC15: Admit New Athlete
 * Masks the complexity of the multi-step admission process.
 * The Administrator interacts only with this single interface,
 * completely shielded from AccountService, ProfileService,
 * and SessionService internals.
 *
 * Implements atomic transaction with rollback:
 * Step 1 — Create user account (AccountService)
 * Step 2 — Initialize athlete profile (ProfileService)
 * Step 3 — Schedule initial evaluation (SessionService)
 * If any step fails, all previous steps are rolled back.
 */
@Service
public class ApexAdmissionFacade implements AdmissionFacade {

    private final AccountService accountService;
    private final ProfileService profileService;
    private final SessionService sessionService;
    private final NotificationEngine notificationEngine;

    // Spring injects all dependencies — DIP in action
    public ApexAdmissionFacade(AccountService accountService,
                                ProfileService profileService,
                                SessionService sessionService,
                                NotificationEngine notificationEngine) {
        this.accountService      = accountService;
        this.profileService      = profileService;
        this.sessionService      = sessionService;
        this.notificationEngine  = notificationEngine;
    }

    /**
     * Orchestrates the full admission workflow as one atomic operation.
     * Client (AdminController) calls this single method only.
     */
    @Override
    public Athlete admitNewAthlete(String username, String password,
                                   String email, String fullName,
                                   int therapistId, int facilityId) {
        User createdUser = null;
        Athlete athleteProfile = null;

        try {
            // Step 1 — Security: Create user account
            createdUser = accountService.createAccount(
                    username, password, email, fullName, Role.ATHLETE);
            int athleteId = createdUser.getUserId();

            // Step 2 — Clinical: Initialize athlete profile
            Athlete athlete = (Athlete) createdUser;
            profileService.createAthleteProfile(athlete);
            athleteProfile = athlete;

            // Step 3 — Scheduling: Book initial evaluation session
            sessionService.scheduleInitialSession(
                    athleteId, therapistId, facilityId);

            // UC21 — Notify all observers of new admission
            notificationEngine.notifyAllObservers(
                    "New athlete admitted: " + fullName +
                    " (ID:" + athleteId + "). " +
                    "Initial evaluation session scheduled.");

            return athlete;

        } catch (Exception e) {
            // Atomic rollback — prevent orphaned records
            rollBackAdmission(
                    createdUser != null ? createdUser.getUserId() : -1);
            throw new RuntimeException(
                    "Admission failed, rolled back: " + e.getMessage(), e);
        }
    }

    /**
     * Cascading rollback — ensures no orphaned records in MySQL.
     * Called automatically on failure.
     */
    @Override
    public void rollBackAdmission(int userId) {
        if (userId <= 0) return;
        try {
            sessionService.cancelInitialSession(userId);
            profileService.deleteProfile(userId);
            accountService.deleteAccount(userId);
        } catch (Exception e) {
            System.err.println("Rollback warning: " + e.getMessage());
        }
    }

    @Override
    public String getAdmissionStatus(int athleteId) {
        return profileService.getProfile(athleteId)
                .map(a -> "ADMITTED - " + a.getFullName())
                .orElse("NOT_FOUND");
    }
}
