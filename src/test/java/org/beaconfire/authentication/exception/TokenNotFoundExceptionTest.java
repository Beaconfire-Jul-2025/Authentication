package org.beaconfire.authentication.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenNotFoundExceptionTest {

    @Test
    public void testTokenNotFoundException() {
        TokenNotFoundException ex = new TokenNotFoundException("Token not found");
        assertEquals("Token not found", ex.getMessage());
    }
}
