package org.beaconfire.authentication.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserNotFoundExceptionTest {
    @Test
    public void testUserNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        assertEquals("User not found", ex.getMessage());
    }
}
