package org.beaconfire.authentication.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthFilter extends OncePerRequestFilter {
    @Value("${auth.shared-key}")
    private String sharedKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if ("/login".equals(path) || "/auth/token".equals(path) || "/auth/register".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (!sharedKey.equals(authHeader)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid auth key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
