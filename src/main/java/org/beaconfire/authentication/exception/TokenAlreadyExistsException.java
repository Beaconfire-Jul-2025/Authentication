package org.beaconfire.authentication.exception;

public class TokenAlreadyExistsException extends RuntimeException {
    public TokenAlreadyExistsException(String message) {
        super(message);
    }
}
