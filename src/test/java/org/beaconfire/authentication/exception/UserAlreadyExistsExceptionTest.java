package org.beaconfire.authentication.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserAlreadyExistsExceptionTest {
    @Test
    public void testUserAlreadyExistsException() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User already exists");
        assertEquals("User already exists", ex.getMessage());
    }
}
