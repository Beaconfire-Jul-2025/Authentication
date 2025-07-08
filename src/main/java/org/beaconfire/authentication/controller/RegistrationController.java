package org.beaconfire.authentication.controller;

import lombok.AllArgsConstructor;
import org.beaconfire.authentication.dto.user.UserRegistration;
import org.beaconfire.authentication.exception.UserAlreadyExistsException;
import org.beaconfire.authentication.service.UserService;
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

    private UserService userService;

    @PostMapping
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistration userRegistration) {
        try {
            userService.registerUser(userRegistration);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "New Buy Registered");
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username or Email Already Exists.");
        }
    }
}
