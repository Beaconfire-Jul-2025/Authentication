package org.beaconfire.authentication.service;

import org.beaconfire.authentication.exception.TokenAlreadyExistsException;
import org.beaconfire.authentication.exception.TokenExpiredException;
import org.beaconfire.authentication.exception.UserNotFoundException;
import org.beaconfire.authentication.model.RegistrationToken;
import org.beaconfire.authentication.model.User;
import org.beaconfire.authentication.repository.RegistrationTokenRepository;
import org.beaconfire.authentication.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class RegistrationTokenServiceTest {

    @Mock
    private RegistrationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegistrationTokenService registrationTokenService;

    @Test
    void testGenerateToken_Success() {
        // Given
        String email = "test@company.com";
        String hrUsername = "hr_user";
        User hrUser = User.builder().id(1).username(hrUsername).build();

        when(userRepository.findByUsername(hrUsername)).thenReturn(Optional.of(hrUser));
        when(tokenRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

        // Create a mock saved token to return
        RegistrationToken savedToken = RegistrationToken.builder()
                .id(1)
                .token("generated-token")
                .email(email)
                .expirationDate(LocalDateTime.now().plusHours(3))
                .createdBy(hrUser)
                .build();

        when(tokenRepository.save(any(RegistrationToken.class))).thenReturn(savedToken);

        // When
        RegistrationToken result = registrationTokenService.generateToken(email, hrUsername);

        // Then
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getCreatedBy()).isEqualTo(hrUser);

        // Verify interactions
        verify(userRepository).findByUsername(hrUsername);
        verify(tokenRepository).findByEmail(email);
        verify(tokenRepository).save(any(RegistrationToken.class));
    }

    @Test
    void testGenerateToken_UserNotFound() {
        // Given
        String email = "test@company.com";
        String hrUsername = "hr_user";

        when(userRepository.findByUsername(hrUsername)).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> registrationTokenService.generateToken(email, hrUsername));

        assertThat(exception.getMessage()).contains("HR user not found with username: " + hrUsername);

        verify(userRepository).findByUsername(hrUsername);
        verify(tokenRepository, never()).findValidTokenByEmail(anyString(), any(LocalDateTime.class));
        verify(tokenRepository, never()).save(any(RegistrationToken.class));
    }

    @Test
    void testGenerateToken_TokenAlreadyExists() {
        // Given
        String email = "test@company.com";
        String hrUsername = "hr_user";
        User hrUser = User.builder().id(1).username(hrUsername).build();

        when(userRepository.findByUsername(hrUsername)).thenReturn(Optional.of(hrUser));

        // Mock: existing valid token found
        RegistrationToken existingToken = RegistrationToken.builder()
                .id(1)
                .token("existing-token")
                .email(email)
                .expirationDate(LocalDateTime.now().plusHours(2))
                .createdBy(hrUser)
                .build();

        when(tokenRepository.findByEmail(email)).thenReturn(Optional.of(existingToken));

        // When & Then
        TokenAlreadyExistsException exception = assertThrows(TokenAlreadyExistsException.class,
                () -> registrationTokenService.generateToken(email, hrUsername));

        assertThat(exception.getMessage()).contains("A token for this email already exists and is valid.");

        // Verify that save was never called
        verify(tokenRepository, never()).save(any(RegistrationToken.class));
    }

    @Test
    void testValidateToken_Success() {
        // Given
        String tokenValue = "valid-token";
        RegistrationToken token = RegistrationToken.builder()
                .token(tokenValue)
                .expirationDate(LocalDateTime.now().plusHours(1))
                .build();

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        // When
        RegistrationToken result = registrationTokenService.validateToken(tokenValue);

        // Then
        assertThat(result).isEqualTo(token);
        verify(tokenRepository).findByToken(tokenValue);
    }


    @Test
    void testValidateToken_TokenExpired() {
        // Given
        RegistrationToken expiredToken = RegistrationToken.builder()
                .token("expired")
                .expirationDate(LocalDateTime.now().minusHours(1))
                .build();

        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(expiredToken));

        // When & Then
        TokenExpiredException exception = assertThrows(TokenExpiredException.class,
                () -> registrationTokenService.validateToken("expired"));

        assertThat(exception.getMessage()).contains("Token is expired at " + expiredToken.getExpirationDate());
        verify(tokenRepository).findByToken("expired");
    }
}
