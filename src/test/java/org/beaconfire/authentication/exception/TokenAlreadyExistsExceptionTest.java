package org.beaconfire.authentication.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenAlreadyExistsExceptionTest {
    @Test
    public void testTokenAlreadyExistsException() {
        TokenAlreadyExistsException ex = new TokenAlreadyExistsException("Token already exists");
        assertEquals("Token already exists", ex.getMessage());
    }
}
