package org.beaconfire.authentication.dto.auth;

import lombok.Builder;
import lombok.Data;
import org.beaconfire.authentication.dto.user.UserResponse;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UserResponse user;
}
