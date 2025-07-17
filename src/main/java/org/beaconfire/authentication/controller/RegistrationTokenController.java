package org.beaconfire.authentication.controller;

import lombok.AllArgsConstructor;
import org.beaconfire.authentication.dto.request.TokenGenerationRequest;
import org.beaconfire.authentication.dto.response.TokenResponse;
import org.beaconfire.authentication.model.RegistrationToken;
import org.beaconfire.authentication.service.RegistrationTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@AllArgsConstructor
public class RegistrationTokenController {
    private final RegistrationTokenService registrationTokenService;

    @PostMapping("/token")
    @PreAuthorize("hasRole('COMPOSITE')")
    public ResponseEntity<TokenResponse> generateToken(
            @Valid @RequestBody TokenGenerationRequest tokenRequest) {

        // Generate the token
        RegistrationToken registrationToken = registrationTokenService.generateToken(
                tokenRequest.getEmail(), tokenRequest.getUserId());

        TokenResponse tokenResponse = TokenResponse.builder()
                .token(registrationToken.getToken())
                .expiration(registrationToken.getExpirationDate())
                .message("Registration token generated and sent via email.")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }
}