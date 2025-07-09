package org.beaconfire.authentication.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.beaconfire.authentication.dto.auth.AuthRequest;
import org.beaconfire.authentication.dto.auth.AuthResponse;
import org.beaconfire.authentication.dto.user.UserResponse;
import org.beaconfire.authentication.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@AllArgsConstructor
@Slf4j
public class LoginController {
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider tokenProvider;

    /**
     * Authenticates a user and returns a JWT.
     *
     * @param authRequest The request body containing username and password.
     * @return A ResponseEntity containing the JWT.
     */
    @PostMapping
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            UserDetails user = (UserDetails) authentication.getPrincipal();
            UserResponse userResponse = UserResponse.builder()
                    .username(user.getUsername())
                    .role(user.getAuthorities().iterator().next().getAuthority())
                    .build();

            AuthResponse response = AuthResponse.builder().token(jwt).user(userResponse).build();
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invalid username or password.");
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Authentication failed due to server error.");
        }
    }
}
