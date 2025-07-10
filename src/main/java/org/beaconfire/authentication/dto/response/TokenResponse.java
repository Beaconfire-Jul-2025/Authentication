package org.beaconfire.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {
    private boolean success;
    private String token;
    private String error;
    private LocalDateTime expirationDate;
    private String message;
}
