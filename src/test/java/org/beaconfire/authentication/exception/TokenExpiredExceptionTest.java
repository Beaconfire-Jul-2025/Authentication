package org.beaconfire.authentication.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenExpiredExceptionTest {
    @Test
    public void testTokenExpiredException() {
        TokenExpiredException ex = new TokenExpiredException("Token expired");
        assertEquals("Token expired", ex.getMessage());
    }
}
