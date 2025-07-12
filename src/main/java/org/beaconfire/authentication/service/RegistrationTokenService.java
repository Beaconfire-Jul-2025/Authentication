package org.beaconfire.authentication.service;

import org.beaconfire.authentication.exception.TokenAlreadyExistsException;
import org.beaconfire.authentication.exception.TokenExpiredException;
import org.beaconfire.authentication.exception.TokenNotFoundException;
import org.beaconfire.authentication.exception.UserNotFoundException;
import org.beaconfire.authentication.model.RegistrationToken;
import org.beaconfire.authentication.model.User;
import org.beaconfire.authentication.repository.RegistrationTokenRepository;
import org.beaconfire.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class RegistrationTokenService {

    private final RegistrationTokenRepository registrationTokenRepository;
    private final UserRepository userRepository;
    @Value("${registration.token.expiration.hours}")
    private int tokenExpirationHours;

    public RegistrationTokenService(RegistrationTokenRepository registrationTokenRepository, UserRepository userRepository) {
        this.registrationTokenRepository = registrationTokenRepository;
        this.userRepository = userRepository;
    }

    public RegistrationToken generateToken(String email, String createdByName) {

        // Find which HR created the token
        User hrUser = userRepository.findByUsername(createdByName)
                .orElseThrow(() -> new UserNotFoundException("HR user not found with username: " + createdByName));

        // Check if a token for this email already exists and is valid
        Optional<RegistrationToken> existingToken = registrationTokenRepository.findByEmail(email);
        if (existingToken.isPresent() && !existingToken.get().isExpired()) {
            throw new TokenAlreadyExistsException("A token for this email already exists and is valid.");
        }

        // Create a new token using UUID
        String token = java.util.UUID.randomUUID().toString();
        LocalDateTime createDate = LocalDateTime.now();
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(tokenExpirationHours);

        // Build the token using the parameters
        RegistrationToken registrationToken = RegistrationToken.builder()
                .token(token)
                .email(email)
                .expirationDate(expirationDate)
                .createDate(createDate)
                .createdBy(hrUser)
                .build();

        return registrationTokenRepository.save(registrationToken);
    }

    public RegistrationToken validateToken(String token) {
        Optional<RegistrationToken> registrationToken = registrationTokenRepository.findByToken(token);

        // Check if the token exists
        if (!registrationToken.isPresent()) {
            throw new TokenNotFoundException("Token not found.");
        }

        // Check if the token is expired
        if (registrationToken.get().isExpired()) {
            throw new TokenExpiredException("Token is expired at " + registrationToken.get().getExpirationDate());
        }

        return registrationToken.get();
    }

    // Clean up expired tokens
    public void cleanupExpiredTokens() {
        registrationTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
