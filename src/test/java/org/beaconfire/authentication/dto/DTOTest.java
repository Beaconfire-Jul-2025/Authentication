package org.beaconfire.authentication.dto;

import org.beaconfire.authentication.dto.request.TokenGenerationRequest;
import org.beaconfire.authentication.dto.response.ErrorResponse;
import org.beaconfire.authentication.dto.response.RegistrationResponse;
import org.beaconfire.authentication.dto.response.TokenResponse;
import org.beaconfire.authentication.dto.user.UserRegistration;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DTOTest {

    @Test
    void testTokenGenerationRequest() {
        // Test builder and getters
        TokenGenerationRequest request = TokenGenerationRequest.builder()
                .email("test@example.com")
                .build();

        assertThat(request.getEmail()).isEqualTo("test@example.com");

        // Test setters
        request.setEmail("new@example.com");
        assertThat(request.getEmail()).isEqualTo("new@example.com");

        // Test equals and hashCode
        TokenGenerationRequest request2 = TokenGenerationRequest.builder()
                .email("new@example.com")
                .build();
        assertThat(request).isEqualTo(request2);
        assertThat(request.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void testTokenResponse() {
        TokenResponse response = TokenResponse.builder()
                .token("abc123")
                .expiration(LocalDateTime.now())
                .message("Success")
                .build();

        assertThat(response.getToken()).isEqualTo("abc123");
        assertThat(response.getMessage()).isEqualTo("Success");
        assertThat(response.getExpiration()).isNotNull();
    }

    @Test
    void testUserRegistration() {
        UserRegistration registration = UserRegistration.builder()
                .token("token123")
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .build();

        assertThat(registration.getUsername()).isEqualTo("testuser");
        assertThat(registration.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testRegistrationResponse() {
        RegistrationResponse response = RegistrationResponse.builder()
                .success(true)
                .message("Registration successful")
                .username("testuser")
                .email("test@example.com")
                .build();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Registration successful");
    }

    @Test
    void testErrorResponse() {
        ErrorResponse error = ErrorResponse.builder()
                .message("Error occurred")
                .build();

        assertThat(error.getMessage()).isEqualTo("Error occurred");
    }
}
