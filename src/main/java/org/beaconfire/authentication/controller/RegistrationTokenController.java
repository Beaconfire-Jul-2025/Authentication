package org.beaconfire.authentication.controller;

import org.beaconfire.authentication.dto.request.TokenGenerationRequest;
import org.beaconfire.authentication.dto.response.TokenResponse;
import org.beaconfire.authentication.model.RegistrationToken;
import org.beaconfire.authentication.service.RegistrationTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/registration-token")
@PreAuthorize("hasRole('HR')")
public class RegistrationTokenController {
    private final RegistrationTokenService registrationTokenService;

    public RegistrationTokenController(RegistrationTokenService registrationTokenService) {
        this.registrationTokenService = registrationTokenService;
    }

    @PostMapping("/generate")
    public ResponseEntity<TokenResponse> generateToken(
            @Valid @RequestBody TokenGenerationRequest tokenRequest,
            Authentication authentication) {

        // Get HR username from authentication
        String hrUsername = authentication.getName();

        // Generate the token
        RegistrationToken registrationToken = registrationTokenService.generateToken(
                tokenRequest.getEmail(), hrUsername);

        TokenResponse tokenResponse = TokenResponse.builder()
                .success(true)
                .token(registrationToken.getToken())
                .expirationDate(registrationToken.getExpirationDate())
                .message("Token generated successfully and email sent successfully.")
                .build();

        return ResponseEntity.ok(tokenResponse);
    }
}