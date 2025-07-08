package org.beaconfire.authentication.service;

import lombok.AllArgsConstructor;
import org.beaconfire.authentication.model.User;
import org.beaconfire.authentication.repository.UserRepository;
import org.beaconfire.authentication.security.CustomUserPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    /**
     * Loads a user by their username and maps their integer role to Spring Security authorities.
     *
     * @param username The username to look for.
     * @return A UserDetails object for Spring Security containing username, password, and authorities.
     * @throws UsernameNotFoundException if the user is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (!user.getActiveFlag()) {
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }

        return new CustomUserPrincipal(user);
    }
}
