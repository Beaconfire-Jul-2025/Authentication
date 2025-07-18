package org.beaconfire.authentication.service;

import org.beaconfire.authentication.exception.TokenAlreadyExistsException;
import org.beaconfire.authentication.exception.TokenExpiredException;
import org.beaconfire.authentication.exception.TokenNotFoundException;
import org.beaconfire.authentication.exception.UserNotFoundException;
import org.beaconfire.authentication.model.RegistrationToken;
import org.beaconfire.authentication.model.User;
import org.beaconfire.authentication.repository.RegistrationTokenRepository;
import org.beaconfire.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationTokenServiceTest {

    @Mock
    private RegistrationTokenRepository registrationTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegistrationTokenService registrationTokenService;

    private User mockHrUser;
    private RegistrationToken mockToken;
    private final String testEmail = "test@example.com";
    private final Integer testUserId = 1;
    private final String testTokenString = "test-token-123";

    @BeforeEach
    void setUp() {
        // Set the token expiration hours value
        ReflectionTestUtils.setField(registrationTokenService, "tokenExpirationHours", 24);

        // Create mock HR user
        mockHrUser = new User();
        mockHrUser.setId(testUserId);
        mockHrUser.setEmail("hr@example.com");

        // Create mock token
        mockToken = RegistrationToken.builder()
                .token(testTokenString)
                .email(testEmail)
                .expirationDate(LocalDateTime.now().plusHours(24))
                .createDate(LocalDateTime.now())
                .createdBy(mockHrUser)
                .build();
    }

    @Test
    void generateToken_Success() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockHrUser));
        when(registrationTokenRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(registrationTokenRepository.save(any(RegistrationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RegistrationToken result = registrationTokenService.generateToken(testEmail, testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testEmail, result.getEmail());
        assertEquals(mockHrUser, result.getCreatedBy());
        assertNotNull(result.getToken());
        assertNotNull(result.getCreateDate());
        assertNotNull(result.getExpirationDate());

        // Verify token is valid for 24 hours
        assertTrue(result.getExpirationDate().isAfter(LocalDateTime.now().plusHours(23)));
        assertTrue(result.getExpirationDate().isBefore(LocalDateTime.now().plusHours(25)));

        verify(userRepository).findById(testUserId);
        verify(registrationTokenRepository).findByEmail(testEmail);
        verify(registrationTokenRepository).save(any(RegistrationToken.class));
    }

    @Test
    void generateToken_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> registrationTokenService.generateToken(testEmail, testUserId)
        );

        assertEquals("HR user not found with username: " + testUserId, exception.getMessage());
        verify(userRepository).findById(testUserId);
        verify(registrationTokenRepository, never()).findByEmail(anyString());
        verify(registrationTokenRepository, never()).save(any());
    }

    @Test
    void generateToken_TokenAlreadyExists_ThrowsException() {
        // Given
        RegistrationToken existingToken = RegistrationToken.builder()
                .token("existing-token")
                .email(testEmail)
                .expirationDate(LocalDateTime.now().plusHours(1)) // Not expired
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockHrUser));
        when(registrationTokenRepository.findByEmail(testEmail)).thenReturn(Optional.of(existingToken));

        // When & Then
        TokenAlreadyExistsException exception = assertThrows(
                TokenAlreadyExistsException.class,
                () -> registrationTokenService.generateToken(testEmail, testUserId)
        );

        assertEquals("A token for this email already exists and is valid.", exception.getMessage());
        verify(registrationTokenRepository, never()).save(any());
    }

    @Test
    void generateToken_ExpiredTokenExists_CreatesNewToken() {
        // Given
        RegistrationToken expiredToken = RegistrationToken.builder()
                .token("expired-token")
                .email(testEmail)
                .expirationDate(LocalDateTime.now().minusHours(1)) // Expired
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockHrUser));
        when(registrationTokenRepository.findByEmail(testEmail)).thenReturn(Optional.of(expiredToken));
        when(registrationTokenRepository.save(any(RegistrationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RegistrationToken result = registrationTokenService.generateToken(testEmail, testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testEmail, result.getEmail());
        verify(registrationTokenRepository).save(any(RegistrationToken.class));
    }

    @Test
    void validateToken_Success() {
        // Given
        when(registrationTokenRepository.findByToken(testTokenString)).thenReturn(Optional.of(mockToken));

        // When
        RegistrationToken result = registrationTokenService.validateToken(testTokenString);

        // Then
        assertNotNull(result);
        assertEquals(mockToken, result);
        verify(registrationTokenRepository).findByToken(testTokenString);
    }

    @Test
    void validateToken_TokenNotFound_ThrowsException() {
        // Given
        when(registrationTokenRepository.findByToken(testTokenString)).thenReturn(Optional.empty());

        // When & Then
        TokenNotFoundException exception = assertThrows(
                TokenNotFoundException.class,
                () -> registrationTokenService.validateToken(testTokenString)
        );

        assertEquals("Token not found.", exception.getMessage());
        verify(registrationTokenRepository).findByToken(testTokenString);
    }

    @Test
    void validateToken_TokenExpired_ThrowsException() {
        // Given
        LocalDateTime expiredDate = LocalDateTime.now().minusHours(1);
        RegistrationToken expiredToken = RegistrationToken.builder()
                .token(testTokenString)
                .email(testEmail)
                .expirationDate(expiredDate)
                .build();

        when(registrationTokenRepository.findByToken(testTokenString)).thenReturn(Optional.of(expiredToken));

        // When & Then
        TokenExpiredException exception = assertThrows(
                TokenExpiredException.class,
                () -> registrationTokenService.validateToken(testTokenString)
        );

        assertEquals("Token is expired at " + expiredDate, exception.getMessage());
        verify(registrationTokenRepository).findByToken(testTokenString);
    }

    @Test
    void cleanupExpiredTokens_Success() {
        // Given
        LocalDateTime beforeTime = LocalDateTime.now().minusSeconds(1);

        // When
        registrationTokenService.cleanupExpiredTokens();

        // Then
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(registrationTokenRepository).deleteExpiredTokens(timeCaptor.capture());

        LocalDateTime capturedTime = timeCaptor.getValue();
        assertTrue(capturedTime.isAfter(beforeTime));
        assertTrue(capturedTime.isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void generateToken_VerifySavedTokenProperties() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(mockHrUser));
        when(registrationTokenRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(registrationTokenRepository.save(any(RegistrationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        registrationTokenService.generateToken(testEmail, testUserId);

        // Then
        ArgumentCaptor<RegistrationToken> tokenCaptor = ArgumentCaptor.forClass(RegistrationToken.class);
        verify(registrationTokenRepository).save(tokenCaptor.capture());

        RegistrationToken savedToken = tokenCaptor.getValue();
        assertNotNull(savedToken.getToken());
        assertEquals(testEmail, savedToken.getEmail());
        assertEquals(mockHrUser, savedToken.getCreatedBy());
        assertNotNull(savedToken.getCreateDate());
        assertNotNull(savedToken.getExpirationDate());

        // Verify UUID format (basic check)
        assertTrue(savedToken.getToken().matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"));
    }
}