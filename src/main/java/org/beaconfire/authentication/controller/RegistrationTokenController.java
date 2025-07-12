package org.beaconfire.authentication.controller;

import lombok.AllArgsConstructor;
import org.beaconfire.authentication.dto.request.TokenGenerationRequest;
import org.beaconfire.authentication.dto.response.TokenResponse;
import org.beaconfire.authentication.model.RegistrationToken;
import org.beaconfire.authentication.service.RegistrationTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
public class RegistrationTokenController {
    private final RegistrationTokenService registrationTokenService;

    @PostMapping("/auth/token")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<TokenResponse> generateToken(
            @Valid @RequestBody TokenGenerationRequest tokenRequest,
            Authentication authentication) {

        // Get HR username from authentication
        String hrUsername = authentication.getName();

        // Generate the token
        RegistrationToken registrationToken = registrationTokenService.generateToken(
                tokenRequest.getEmail(), hrUsername);

        TokenResponse tokenResponse = TokenResponse.builder()
                .token(registrationToken.getToken())
                .expiration(registrationToken.getExpirationDate())
                .message("Registration token generated and sent via email.")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }
}