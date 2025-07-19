package org.beaconfire.authentication.controller;

import lombok.RequiredArgsConstructor;
import org.beaconfire.authentication.model.Role;
import org.beaconfire.authentication.model.User;
import org.beaconfire.authentication.model.UserRole;
import org.beaconfire.authentication.repository.RoleRepository;
import org.beaconfire.authentication.repository.UserRepository;
import org.beaconfire.authentication.repository.UserRoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/secure")
@PreAuthorize("hasRole('COMPOSITE')")
@RequiredArgsConstructor
public class SecureController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, String>>> getUserList() {
        List<Map<String, String>> users = userRepository.findAll().stream().map(user -> {
            String role = user.getUserRoles().stream()
                    .findFirst()
                    .map(ur -> ur.getRole().getRoleName())
                    .orElse("");
            Map<String, String> userMap = new HashMap<>();
            userMap.put("userId", user.getId().toString());
            userMap.put("username", user.getUsername());
            userMap.put("role", role);
            return userMap;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .activeFlag(true)
                .build();
        user = userRepository.save(user);
        // Assign default role
        Role defaultRole = roleRepository.findByRoleName("ROLE_ONBOARD")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("ROLE_ONBOARD").roleDescription("Default role").build()));
        UserRole userRole = UserRole.builder().user(user).role(defaultRole).activeFlag(true).build();
        userRoleRepository.save(userRole);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("userId", user.getId().toString());
        responseMap.put("username", user.getUsername());
        responseMap.put("role", defaultRole.getRoleName());
        return ResponseEntity.ok(responseMap);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Map<String, String>> updateUserRole(@PathVariable Integer userId, @RequestBody Map<String, String> request) {
        String roleName = request.get("role");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Role role = roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().roleName(roleName).roleDescription("Custom role").build()));
        // Remove old roles and assign new
        userRoleRepository.deleteAll(user.getUserRoles());
        UserRole userRole = UserRole.builder().user(user).role(role).activeFlag(true).build();
        userRoleRepository.save(userRole);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("userId", user.getId().toString());
        responseMap.put("username", user.getUsername());
        responseMap.put("role", role.getRoleName());
        return ResponseEntity.ok(responseMap);
    }
}
