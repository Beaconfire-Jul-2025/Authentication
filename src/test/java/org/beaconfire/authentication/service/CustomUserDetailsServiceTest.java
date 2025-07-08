package org.beaconfire.authentication.service;

import org.beaconfire.authentication.model.Role;
import org.beaconfire.authentication.model.User;
import org.beaconfire.authentication.model.UserRole;
import org.beaconfire.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsernameHR() {
        String username = "HR";

        Role role = Role.builder()
                .roleName("ROLE_HR")
                .roleDescription("HR role")
                .build();

        User user = User.builder()
                .username(username)
                .password("password")
                .email("admin@example.com")
                .activeFlag(true)
                .build();

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .activeFlag(true)
                .build();

        user.getUserRoles().add(userRole);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertEquals(username, userDetails.getUsername());

        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertEquals(1, authorities.size());
        assertEquals("ROLE_HR", authorities.get(0));
    }

    @Test
    void testLoadUserByUsernameEmployee() {
        String username = "employee";

        Role role = Role.builder()
                .roleName("ROLE_EMPLOYEE")
                .roleDescription("Standard employee role")
                .build();

        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .email("user@example.com")
                .activeFlag(true)
                .build();

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .activeFlag(true)
                .build();

        user.getUserRoles().add(userRole);


        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Then
        assertEquals(username, userDetails.getUsername());
        List<GrantedAuthority> authorities = new ArrayList<>(userDetails.getAuthorities());
        assertEquals("ROLE_EMPLOYEE", authorities.get(0).getAuthority());
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        // Given
        String username = "nonExistentUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username),
                "User not found with username: " + username
        );
    }
}