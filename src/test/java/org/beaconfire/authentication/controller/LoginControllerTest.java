package org.beaconfire.authentication.controller;

import org.beaconfire.authentication.dto.auth.AuthRequest;
import org.beaconfire.authentication.dto.auth.AuthResponse;
import org.beaconfire.authentication.repository.UserRepository;
import org.beaconfire.authentication.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginControllerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider tokenProvider;
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        tokenProvider = mock(JwtTokenProvider.class);
        loginController = new LoginController(authenticationManager, tokenProvider, userRepository);
    }

    @Test
    @Disabled
    void testAuthenticateUser_Success() {
        AuthRequest request = new AuthRequest("user1", "password123");
        Authentication auth = mock(Authentication.class);
        org.springframework.security.core.userdetails.User mockUser =
                new org.springframework.security.core.userdetails.User(
                        "user1", "password123",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        // Mock authentication
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(mockUser);

        // Mock User entity and UserRepository
        org.beaconfire.authentication.model.User entityUser = org.beaconfire.authentication.model.User.builder()
                .id(1)
                .username("user1")
                .email("user1@email.com")
                .password("password123")
                .build();
        org.beaconfire.authentication.model.Role role = new org.beaconfire.authentication.model.Role();
        role.setRoleName("ROLE_USER");
        org.beaconfire.authentication.model.UserRole userRole = new org.beaconfire.authentication.model.UserRole();
        userRole.setRole(role);
        userRole.setUser(entityUser);
        entityUser.getUserRoles().add(userRole);
        when(userRepository.findByUsername("user1")).thenReturn(java.util.Optional.of(entityUser));

        // Mock token provider
        when(tokenProvider.generateTokenWithClaims(any())).thenReturn("mock-jwt");

        ResponseEntity<?> response = loginController.authenticateUser(request);

        assertEquals(200, response.getStatusCodeValue());
        assertInstanceOf(AuthResponse.class, response.getBody());
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("mock-jwt", authResponse.getToken());
        assertEquals("user1", authResponse.getUser().getUsername());
        assertEquals("ROLE_USER", authResponse.getUser().getRole());
    }

    @Test
    void testAuthenticateUser_InvalidCredentials() {
        AuthRequest request = new AuthRequest("user1", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<?> response = loginController.authenticateUser(request);

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Invalid username or password.", response.getBody());
    }

    @Test
    void testAuthenticateUser_OtherAuthenticationException() {
        AuthRequest request = new AuthRequest("user1", "password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new AuthenticationServiceException("Service unavailable"));

        ResponseEntity<?> response = loginController.authenticateUser(request);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Authentication failed due to server error.", response.getBody());
    }
}
