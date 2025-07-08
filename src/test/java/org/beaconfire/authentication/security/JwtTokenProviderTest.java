package org.beaconfire.authentication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JwtTokenProviderTest {

    private final String testJwtSecret = "NiXTYNwyutkwyp34w3TYjb297yYUZCaCmr3YhDdT0W4=";
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    public void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtTokenProvider.setJwtExpirationInMs(3600000);
        jwtTokenProvider.setJwtSecret("NiXTYNwyutkwyp34w3TYjb297yYUZCaCmr3YhDdT0W4=");
    }

    @Test
    void testGenerateToken() throws Exception {
        // Arrange
        User user = new User("testUser", "password", Collections.emptyList());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        // Act
        String token = jwtTokenProvider.generateToken(authentication);

        // Assert
        assertNotNull(token);

        // Parse the token and validate the claims
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testJwtSecret));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("testUser", claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
    }
}
