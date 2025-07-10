package org.beaconfire.authentication.service;

import org.beaconfire.authentication.exception.TokenAlreadyExistsException;
import org.beaconfire.authentication.exception.TokenExpiredException;
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
        when(tokenRepository.findValidTokenByEmail(eq(email), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(tokenRepository.save(any(RegistrationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RegistrationToken result = registrationTokenService.generateToken(email, hrUsername);

        // Then
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getToken()).isNotNull();
        verify(tokenRepository).save(any(RegistrationToken.class));
    }

    @Test
    void testGenerateToken_TokenAlreadyExists() {
        // Given
        String email = "test@company.com";
        String hrUsername = "hr_user";
        User hrUser = User.builder().id(1).username(hrUsername).build();
        RegistrationToken existingToken = RegistrationToken.builder().build();

        when(userRepository.findByUsername(hrUsername)).thenReturn(Optional.of(hrUser));
        when(tokenRepository.findValidTokenByEmail(eq(email), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existingToken));

        // When & Then
        assertThrows(TokenAlreadyExistsException.class,
                () -> registrationTokenService.generateToken(email, hrUsername));
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
        assertThrows(TokenExpiredException.class,
                () -> registrationTokenService.validateToken("expired"));
    }
}
