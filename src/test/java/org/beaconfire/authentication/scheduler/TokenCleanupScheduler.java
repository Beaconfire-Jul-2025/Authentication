package org.beaconfire.authentication.scheduler;

import org.beaconfire.authentication.service.RegistrationTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenCleanupSchedulerTest {

    @Mock
    private RegistrationTokenService registrationTokenService;

    @InjectMocks
    private TokenCleanupScheduler tokenCleanupScheduler;

    @Test
    void testCleanupExpiredTokens() {
        // When
        tokenCleanupScheduler.cleanupExpiredTokens();

        // Then
        verify(registrationTokenService, times(1)).cleanupExpiredTokens();
    }

    @Test
    void testScheduledAnnotationExists() {
        // This test verifies that the method has the @Scheduled annotation
        // In a real scenario, you might want to test the scheduler configuration

        try {
            java.lang.reflect.Method method = TokenCleanupScheduler.class.getMethod("cleanupExpiredTokens");
            org.springframework.scheduling.annotation.Scheduled scheduledAnnotation =
                    method.getAnnotation(org.springframework.scheduling.annotation.Scheduled.class);

            // Verify that the @Scheduled annotation is present
            assertNotNull(scheduledAnnotation);

        } catch (NoSuchMethodException e) {
            fail("cleanupExpiredTokens method not found");
        }
    }

    @Test
    void testMultipleInvocations() {
        // When
        tokenCleanupScheduler.cleanupExpiredTokens();
        tokenCleanupScheduler.cleanupExpiredTokens();
        tokenCleanupScheduler.cleanupExpiredTokens();

        // Then
        verify(registrationTokenService, times(3)).cleanupExpiredTokens();
    }

    @Test
    void testCleanupExpiredTokens_VerifyNoOtherInteractions() {
        // When
        tokenCleanupScheduler.cleanupExpiredTokens();

        // Then
        verify(registrationTokenService, times(1)).cleanupExpiredTokens();
        verifyNoMoreInteractions(registrationTokenService);
    }
}