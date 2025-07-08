package org.beaconfire.authentication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.beaconfire.authentication.dto.user.UserRegistration;
import org.beaconfire.authentication.exception.UserAlreadyExistsException;
import org.beaconfire.authentication.model.Role;
import org.beaconfire.authentication.model.User;
import org.beaconfire.authentication.model.UserRole;
import org.beaconfire.authentication.repository.RoleRepository;
import org.beaconfire.authentication.repository.UserRepository;
import org.beaconfire.authentication.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ROLE_NAME = "ROLE_EMPLOYEE";

    @Transactional
    public void registerUser(UserRegistration registration) throws UserAlreadyExistsException {
        if (userRepository.existsByUsername(registration.getUsername())) {
            log.warn("Registration attempt with existing username: {}", registration.getUsername());
            throw new UserAlreadyExistsException(
                    "User with email '" + registration.getEmail() + "' already exists.");
        }

        if (userRepository.existsByEmail(registration.getEmail())) {
            log.warn("Registration attempt with existing email: {}", registration.getEmail());
            throw new UserAlreadyExistsException(
                    "User with email '" + registration.getEmail() + "' already exists.");
        }

        User user = User.builder()
                .username(registration.getUsername())
                .email(registration.getEmail())
                .password(passwordEncoder.encode(registration.getPassword()))
                .activeFlag(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User {} created successfully with ID: {}", registration.getUsername(), savedUser.getId());

        // Assign default role to the user
        assignDefaultRoleToUser(savedUser);

        log.info("User {} registered successfully with default role.", registration.getUsername());
    }

    /**
     * Assigns the default role to a newly registered user.
     * Creates the default role if it doesn't exist.
     *
     * @param user The user to assign the role to
     */
    private void assignDefaultRoleToUser(User user) {
        // Find or create the default role
        Role defaultRole = roleRepository.findByRoleName(DEFAULT_ROLE_NAME)
                .orElseGet(() -> {
                    log.info("Default role '{}' not found. Creating it.", DEFAULT_ROLE_NAME);
                    Role newRole = Role.builder()
                            .roleName(DEFAULT_ROLE_NAME)
                            .roleDescription("Default role for registered users")
                            .build();
                    return roleRepository.save(newRole);
                });

        // Create user-role association
        UserRole userRole = UserRole.builder()
                .user(user)
                .role(defaultRole)
                .activeFlag(true)
                .build();

        userRoleRepository.save(userRole);
        log.info("Assigned role '{}' to user '{}'", DEFAULT_ROLE_NAME, user.getUsername());
    }
}
