package org.beaconfire.authentication.service;

import org.beaconfire.authentication.dto.user.UserRegistration;
import org.beaconfire.authentication.exception.UserAlreadyExistsException;
import org.beaconfire.authentication.model.Role;
import org.beaconfire.authentication.model.User;
import org.beaconfire.authentication.model.UserRole;
import org.beaconfire.authentication.repository.RoleRepository;
import org.beaconfire.authentication.repository.UserRepository;
import org.beaconfire.authentication.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistration testRegistration;
    private User savedUser;
    private Role defaultRole;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded_password123";
    private static final String DEFAULT_ROLE_NAME = "ROLE_EMPLOYEE";

    @BeforeEach
    void setUp() {
        // Create test registration object
        testRegistration = new UserRegistration();
        testRegistration.setUsername(TEST_USERNAME);
        testRegistration.setEmail(TEST_EMAIL);
        testRegistration.setPassword(TEST_PASSWORD);

        // Create saved user object
        savedUser = User.builder()
                .id(1)
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .activeFlag(true)
                .build();

        // Create default role
        defaultRole = Role.builder()
                .id(1)
                .roleName(DEFAULT_ROLE_NAME)
                .roleDescription("Default role for registered users")
                .build();
    }

    @Test
    void registerUser_Success_WithExistingRole() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(roleRepository.findByRoleName(DEFAULT_ROLE_NAME)).thenReturn(Optional.of(defaultRole));
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> userService.registerUser(testRegistration));

        // Then
        // Verify user creation
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(TEST_USERNAME, capturedUser.getUsername());
        assertEquals(TEST_EMAIL, capturedUser.getEmail());
        assertEquals(ENCODED_PASSWORD, capturedUser.getPassword());
        assertTrue(capturedUser.getActiveFlag());

        // Verify role assignment
        ArgumentCaptor<UserRole> userRoleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(userRoleCaptor.capture());
        UserRole capturedUserRole = userRoleCaptor.getValue();
        assertEquals(savedUser, capturedUserRole.getUser());
        assertEquals(defaultRole, capturedUserRole.getRole());
        assertTrue(capturedUserRole.getActiveFlag());

        // Verify method calls
        verify(userRepository).existsByUsername(TEST_USERNAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(roleRepository).findByRoleName(DEFAULT_ROLE_NAME);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void registerUser_Success_CreatesNewRole() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(roleRepository.findByRoleName(DEFAULT_ROLE_NAME)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(defaultRole);
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> userService.registerUser(testRegistration));

        // Then
        // Verify role creation
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(roleCaptor.capture());
        Role capturedRole = roleCaptor.getValue();
        assertEquals(DEFAULT_ROLE_NAME, capturedRole.getRoleName());
        assertEquals("Default role for registered users", capturedRole.getRoleDescription());

        // Verify user and user-role creation
        verify(userRepository).save(any(User.class));
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void registerUser_ThrowsException_WhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        // When & Then
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(testRegistration)
        );

        assertEquals("User with email '" + TEST_EMAIL + "' already exists.", exception.getMessage());

        // Verify that no user was saved
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(roleRepository, never()).findByRoleName(anyString());
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void registerUser_ThrowsException_WhenEmailExists() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // When & Then
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(testRegistration)
        );

        assertEquals("User with email '" + TEST_EMAIL + "' already exists.", exception.getMessage());

        // Verify that no user was saved
        verify(userRepository).existsByUsername(TEST_USERNAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(roleRepository, never()).findByRoleName(anyString());
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void registerUser_TransactionalBehavior_RollbackOnError() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(roleRepository.findByRoleName(DEFAULT_ROLE_NAME)).thenReturn(Optional.of(defaultRole));
        // Simulate exception during user-role save
        when(userRoleRepository.save(any(UserRole.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.registerUser(testRegistration));

        // Verify all operations were called but transaction should rollback
        verify(userRepository).save(any(User.class));
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void registerUser_VerifyUserBuilderProperties() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(roleRepository.findByRoleName(DEFAULT_ROLE_NAME)).thenReturn(Optional.of(defaultRole));

        // When
        assertDoesNotThrow(() -> userService.registerUser(testRegistration));

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertAll("User properties",
                () -> assertEquals(TEST_USERNAME, capturedUser.getUsername()),
                () -> assertEquals(TEST_EMAIL, capturedUser.getEmail()),
                () -> assertEquals(ENCODED_PASSWORD, capturedUser.getPassword()),
                () -> assertTrue(capturedUser.getActiveFlag()),
                () -> assertNull(capturedUser.getId()) // ID should be null before save
        );
    }

    @Test
    void registerUser_WithDifferentRegistrationData() {
        // Given - different test data
        UserRegistration differentRegistration = new UserRegistration();
        differentRegistration.setUsername("anotheruser");
        differentRegistration.setEmail("another@example.com");
        differentRegistration.setPassword("differentpass");

        User differentSavedUser = User.builder()
                .id(2)
                .username("anotheruser")
                .email("another@example.com")
                .password("encoded_differentpass")
                .activeFlag(true)
                .build();

        when(userRepository.existsByUsername("anotheruser")).thenReturn(false);
        when(userRepository.existsByEmail("another@example.com")).thenReturn(false);
        when(passwordEncoder.encode("differentpass")).thenReturn("encoded_differentpass");
        when(userRepository.save(any(User.class))).thenReturn(differentSavedUser);
        when(roleRepository.findByRoleName(DEFAULT_ROLE_NAME)).thenReturn(Optional.of(defaultRole));

        // When
        assertDoesNotThrow(() -> userService.registerUser(differentRegistration));

        // Then
        verify(userRepository).existsByUsername("anotheruser");
        verify(userRepository).existsByEmail("another@example.com");
        verify(passwordEncoder).encode("differentpass");
    }

    @Test
    void registerUser_VerifyOrderOfOperations() {
        // Given
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(roleRepository.findByRoleName(DEFAULT_ROLE_NAME)).thenReturn(Optional.of(defaultRole));

        // When
        assertDoesNotThrow(() -> userService.registerUser(testRegistration));

        // Then - verify order of operations
        InOrder inOrder = inOrder(userRepository, passwordEncoder, roleRepository, userRoleRepository);
        inOrder.verify(userRepository).existsByUsername(TEST_USERNAME);
        inOrder.verify(userRepository).existsByEmail(TEST_EMAIL);
        inOrder.verify(passwordEncoder).encode(TEST_PASSWORD);
        inOrder.verify(userRepository).save(any(User.class));
        inOrder.verify(roleRepository).findByRoleName(DEFAULT_ROLE_NAME);
        inOrder.verify(userRoleRepository).save(any(UserRole.class));
    }
}
