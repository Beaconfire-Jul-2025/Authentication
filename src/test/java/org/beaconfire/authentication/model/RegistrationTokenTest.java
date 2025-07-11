package org.beaconfire.authentication.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RegistrationTokenTest {

    @Test
    void testRegistrationTokenBuilder() {
        User user = User.builder().id(1).username("hr").build();

        RegistrationToken token = RegistrationToken.builder()
                .id(1)
                .token("abc123")
                .email("test@example.com")
                .expirationDate(LocalDateTime.now().plusHours(3))
                .createdBy(user)
                .build();

        assertThat(token.getId()).isEqualTo(1);
        assertThat(token.getToken()).isEqualTo("abc123");
        assertThat(token.getEmail()).isEqualTo("test@example.com");
        assertThat(token.getCreatedBy()).isEqualTo(user);
    }

    @Test
    void testIsExpired() {
        // Test non-expired token
        RegistrationToken validToken = RegistrationToken.builder()
                .token("valid")
                .expirationDate(LocalDateTime.now().plusHours(1))
                .build();

        assertThat(validToken.isExpired()).isFalse();

        // Test expired token
        RegistrationToken expiredToken = RegistrationToken.builder()
                .token("expired")
                .expirationDate(LocalDateTime.now().minusHours(1))
                .build();

        assertThat(expiredToken.isExpired()).isTrue();
    }

    @Test
    void testCustomConstructor() {
        User user = User.builder().id(1).username("hr").build();
        LocalDateTime now = LocalDateTime.now();

        RegistrationToken token = RegistrationToken.builder()
                .token("token123")
                .email("test@example.com")
                .expirationDate(now.plusHours(3))
                .createdBy(user)
                .build();

        assertThat(token.getToken()).isEqualTo("token123");
        assertThat(token.getEmail()).isEqualTo("test@example.com");
        assertThat(token.getCreatedBy()).isEqualTo(user);
    }
}

