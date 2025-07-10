package org.beaconfire.authentication.controller;

import lombok.AllArgsConstructor;
import org.beaconfire.authentication.dto.user.UserRegistration;
import org.beaconfire.authentication.exception.UserAlreadyExistsException;
import org.beaconfire.authentication.model.RegistrationToken;
import org.beaconfire.authentication.repository.RegistrationTokenRepository;
import org.beaconfire.authentication.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/signup")
@AllArgsConstructor
public class RegistrationController {

    private UserService userService;
    private final RegistrationTokenRepository tokenRepository;

    @PostMapping
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistration userRegistration) {
        try {
            userService.registerUser(userRegistration);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "New User Registered");
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username or Email Already Exists.");
        }
    }
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam("token") String token) {
        Optional<RegistrationToken> optionalToken = tokenRepository.findValidTokenByToken(token, LocalDateTime.now());

        if (!optionalToken.isPresent()) {
            return ResponseEntity.badRequest().body("Invalid, expired, or already used token.");
        }

        RegistrationToken regToken = optionalToken.get();
        // Returns a wrapped DTO
        Map<String, Object> response = new HashMap<>();
        response.put("email", regToken.getEmail());
        response.put("valid", true);

        return ResponseEntity.ok(response);
    }
}
