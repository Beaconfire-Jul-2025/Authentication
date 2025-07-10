package org.beaconfire.authentication.controller;

import lombok.AllArgsConstructor;
import org.beaconfire.authentication.dto.user.UserRegistration;
import org.beaconfire.authentication.dto.response.RegistrationResponse;
import org.beaconfire.authentication.service.RegistrationTokenService;
import org.beaconfire.authentication.model.RegistrationToken;
import org.beaconfire.authentication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/signup")
@AllArgsConstructor
public class RegistrationController {

    @Autowired
    private UserService userService;
    @Autowired
    private RegistrationTokenService registrationTokenService;

    @PostMapping
    public ResponseEntity<RegistrationResponse> registerUser(
            @Valid @RequestBody UserRegistration request) {

        // Validate the token
        RegistrationToken token = registrationTokenService.validateToken(request.getToken());

        // Ensure email matches the token
        if (!token.getEmail().equals(request.getEmail())) {
            return ResponseEntity.badRequest().body(RegistrationResponse.builder()
                    .success(false)
                    .message("Email does not match the token.")
                    .build());
        }

        userService.registerUser(request);

        return ResponseEntity.ok(RegistrationResponse.builder()
                .success(true)
                .message("New Employee Registered")
                .username(request.getUsername())
                .email(request.getEmail())
                .build());
    }
}
