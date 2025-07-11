package org.beaconfire.authentication.scheduler;

import org.beaconfire.authentication.service.RegistrationTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenCleanupScheduler {

    private final RegistrationTokenService tokenService;

    public TokenCleanupScheduler(RegistrationTokenService tokenService) {
        this.tokenService = tokenService;
    }

    // Clean up expired tokens every day at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        tokenService.cleanupExpiredTokens();
    }
}